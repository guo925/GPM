package com.gjx.gpms.controller;

import com.gjx.gpms.common.result.Result;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.config.FileStorageProperties;
import com.gjx.gpms.service.FileStorageService;
import com.gjx.gpms.vo.FileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件上传接口。
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileStorageProperties fileStorageProperties;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String bizType
    ) {
        return Result.success(fileStorageService.upload(file, bizType));
    }

    @Operation(summary = "查看文件")
    @GetMapping("/view")
    public ResponseEntity<Resource> view(@RequestParam String path) {
        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        if (decodedPath.startsWith("http://") || decodedPath.startsWith("https://")) {
            return ResponseEntity.status(302).location(URI.create(decodedPath)).build();
        }

        String prefix = normalizePrefix(fileStorageProperties.getLocalUrlPrefix());
        String objectKey = decodedPath;
        if (objectKey.startsWith(prefix + "/")) {
            objectKey = objectKey.substring(prefix.length() + 1);
        } else if (objectKey.startsWith("/")) {
            throw new BusinessException("文件路径不合法");
        }

        try {
            Path root = Path.of(fileStorageProperties.getLocalPath()).toAbsolutePath().normalize();
            Path target = root.resolve(objectKey).normalize();
            if (!target.startsWith(root) || !Files.isRegularFile(target)) {
                throw new BusinessException("文件不存在");
            }

            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            String fileName = target.getFileName().toString();
            return ResponseEntity.ok()
                    .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(fileName, StandardCharsets.UTF_8).build().toString())
                    .body(resource);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文件读取失败");
        }
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/uploads";
        }
        return prefix.startsWith("/") ? prefix : "/" + prefix;
    }
}
