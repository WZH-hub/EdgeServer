package com.example.edgeserver.modules.stickman.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RealTimeImage {
    /**
     * id
     */
    private int id;

    /**
     * box_id box表主键
     */
    private int boxId;

    /**
     * image_url 原图
     */
    private String imageUrl;

    /**
     * combined_image_url 火柴人+原图 拼接
     */
    private String combinedImageUrl;

    /**
     * stickman_image_url 火柴人+背景 拼接
     */
    private String stickmanImageUrl;

    /**
     * time 时间
     */
    private Date time;
}
