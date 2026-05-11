package com.baomidou.service.impl;

import com.baomidou.entity.Review;
import com.baomidou.mapper.ReviewMapper;
import com.baomidou.service.IReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 指导教师审阅记录 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements IReviewService {

}
