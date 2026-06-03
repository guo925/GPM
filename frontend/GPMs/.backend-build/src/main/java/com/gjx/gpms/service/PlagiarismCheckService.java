package com.gjx.gpms.service;

import com.gjx.gpms.dto.PlagiarismCheckDTO;
import com.gjx.gpms.vo.PlagiarismCheckVO;

/**
 * PlagiarismCheck 服务接口。
 */
public interface PlagiarismCheckService {

    /**
     * 检查相关逻辑。
     */
    PlagiarismCheckVO check(PlagiarismCheckDTO dto);
}
