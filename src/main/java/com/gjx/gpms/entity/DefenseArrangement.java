package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 答辩安排
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("defense_arrangement")
@ApiModel(value = "DefenseArrangement对象", description = "答辩安排")
public class DefenseArrangement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long defenseBatchId;

    /**
     * 学生ID
     */
    @ApiModelProperty("学生ID")
    private Long studentId;

    /**
     * 答辩组ID
     */
    @ApiModelProperty("答辩组ID")
    private Long groupId;

    /**
     * 具体答辩时间
     */
    @ApiModelProperty("具体答辩时间")
    private LocalDateTime defenseTime;

    /**
     * 地点
     */
    @ApiModelProperty("地点")
    private String location;

    private LocalDateTime createdAt;
}
