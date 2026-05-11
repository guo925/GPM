package com.baomidou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 指导教师审阅记录
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@ApiModel(value = "Review对象", description = "指导教师审阅记录")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审阅记录ID
     */
    @ApiModelProperty("审阅记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的提交记录ID
     */
    @ApiModelProperty("关联的提交记录ID")
    private Integer submissionId;

    /**
     * 审阅教师ID
     */
    @ApiModelProperty("审阅教师ID")
    private Integer teacherId;

    /**
     * 操作类型：退回修改/确认定稿
     */
    @ApiModelProperty("操作类型：退回修改/确认定稿")
    private String operationType;

    /**
     * 总体修改意见
     */
    @ApiModelProperty("总体修改意见")
    private String overallComment;

    /**
     * 批注文件路径（可选）
     */
    @ApiModelProperty("批注文件路径（可选）")
    private String commentFilePath;

    /**
     * 审阅时间
     */
    @ApiModelProperty("审阅时间")
    private LocalDateTime reviewTime;
}
