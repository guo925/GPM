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
 * 学生文档提交记录
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Data
@ApiModel(value = "Submission对象", description = "学生文档提交记录")
public class Submission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交记录ID
     */
    @ApiModelProperty("提交记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生ID
     */
    @ApiModelProperty("学生ID")
    private Integer studentId;

    /**
     * 批次ID
     */
    @ApiModelProperty("批次ID")
    private Integer batchId;

    /**
     * 关联课题ID
     */
    @ApiModelProperty("关联课题ID")
    private Integer topicId;

    /**
     * 文档存储路径
     */
    @ApiModelProperty("文档存储路径")
    private String filePath;

    /**
     * 版本号（提交次数）
     */
    @ApiModelProperty("版本号（提交次数）")
    private Integer version;

    /**
     * 状态：已提交/退回/定稿
     */
    @ApiModelProperty("状态：已提交/退回/定稿")
    private String status;

    /**
     * 学生备注
     */
    @ApiModelProperty("学生备注")
    private String remark;

    /**
     * 提交时间
     */
    @ApiModelProperty("提交时间")
    private LocalDateTime submitTime;
}
