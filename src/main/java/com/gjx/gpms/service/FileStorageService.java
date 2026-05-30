package com.gjx.gpms.service;

import com.gjx.gpms.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务。
 */
public interface FileStorageService {

    FileUploadVO upload(MultipartFile file, String bizType);
}
