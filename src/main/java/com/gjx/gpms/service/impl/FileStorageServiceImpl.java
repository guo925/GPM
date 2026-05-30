package com.gjx.gpms.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.config.AliyunOssProperties;
import com.gjx.gpms.config.FileStorageProperties;
import com.gjx.gpms.service.FileStorageService;
import com.gjx.gpms.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * 文件存储实现，按配置选择本地或阿里云 OSS。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "wps", "xls", "xlsx", "et", "ppt", "pptx", "dps", "txt", "zip", "rar", "png", "jpg", "jpeg"
    );

    private final FileStorageProperties fileStorageProperties;
    private final AliyunOssProperties aliyunOssProperties;

    @Override
    public FileUploadVO upload(MultipartFile file, String bizType) {
        validate(file);
        String objectKey = buildObjectKey(file.getOriginalFilename(), bizType);
        if ("oss".equalsIgnoreCase(fileStorageProperties.getType())) {
            return uploadToOss(file, objectKey);
        }
        return uploadToLocal(file, objectKey);
    }

    private FileUploadVO uploadToLocal(MultipartFile file, String objectKey) {
        try {
            Path root = Path.of(fileStorageProperties.getLocalPath()).toAbsolutePath().normalize();
            Path target = root.resolve(objectKey).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException("文件路径不合法");
            }
            Files.createDirectories(target.getParent());
            file.transferTo(target);
            return buildVO(file, objectKey, normalizePrefix(fileStorageProperties.getLocalUrlPrefix()) + "/" + objectKey);
        } catch (IOException e) {
            log.error("本地文件上传失败，objectKey={}", objectKey, e);
            throw new BusinessException("文件上传失败");
        }
    }

    private FileUploadVO uploadToOss(MultipartFile file, String objectKey) {
        if (StringUtils.isAnyBlank(
                aliyunOssProperties.getEndpoint(),
                aliyunOssProperties.getBucketName(),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret()
        )) {
            throw new BusinessException("OSS配置不完整");
        }

        OSS ossClient = new OSSClientBuilder().build(
                aliyunOssProperties.getEndpoint(),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret()
        );
        try {
            ossClient.putObject(aliyunOssProperties.getBucketName(), objectKey, file.getInputStream());
            return buildVO(file, objectKey, buildOssUrl(objectKey));
        } catch (IOException e) {
            log.error("OSS文件上传失败，objectKey={}", objectKey, e);
            throw new BusinessException("文件上传失败");
        } finally {
            ossClient.shutdown();
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择上传文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过50MB");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("不支持的文件类型");
        }
    }

    private String buildObjectKey(String originalName, String bizType) {
        String safeBizType = StringUtils.defaultIfBlank(bizType, "common").replaceAll("[^a-zA-Z0-9_-]", "");
        String ext = getExtension(originalName);
        LocalDate now = LocalDate.now();
        return "%s/%d/%02d/%02d/%s.%s".formatted(
                safeBizType,
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                UUID.randomUUID(),
                ext
        );
    }

    private String getExtension(String filename) {
        String name = StringUtils.defaultString(filename);
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return "";
        }
        return name.substring(index + 1).toLowerCase();
    }

    private FileUploadVO buildVO(MultipartFile file, String objectKey, String url) {
        FileUploadVO vo = new FileUploadVO();
        vo.setOriginalName(file.getOriginalFilename());
        vo.setObjectKey(objectKey);
        vo.setUrl(url);
        vo.setSize(file.getSize());
        vo.setContentType(file.getContentType());
        return vo;
    }

    private String buildOssUrl(String objectKey) {
        String endpoint = aliyunOssProperties.getEndpoint().replace("https://", "").replace("http://", "");
        return "https://" + aliyunOssProperties.getBucketName() + "." + endpoint + "/" + objectKey;
    }

    private String normalizePrefix(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return "/uploads";
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }
}
