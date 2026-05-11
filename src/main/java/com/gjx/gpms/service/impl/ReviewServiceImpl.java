package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.Review;
import com.gjx.gpms.mapper.ReviewMapper;
import com.gjx.gpms.service.IReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 指导教师审阅记录 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements IReviewService {

}
