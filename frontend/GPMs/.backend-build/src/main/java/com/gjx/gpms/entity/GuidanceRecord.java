package com.gjx.gpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 指导周记
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Data
@TableName("guidance_record")

public class GuidanceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long studentTopicId;

    /**
     * 第几周
     */
    
    private Integer weekNumber;

    /**
     * 学生填写内容
     */
    
    private String content;

    private String filePath;

    /**
     * draft/submitted/reviewed
     */
    
    private String status;

    private String advisorComment;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
