package com.gjx.gpms.service;

import com.gjx.gpms.dto.PlagiarismCheckDTO;
import com.gjx.gpms.vo.PlagiarismCheckVO;

public interface PlagiarismCheckService {

    PlagiarismCheckVO check(PlagiarismCheckDTO dto);
}
