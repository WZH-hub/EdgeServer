package com.example.edgeserver.service;

import com.example.edgeserver.entity.PoseDetectionResponse;
import com.example.edgeserver.modules.stickman.model.domain.RealTimeImage;

import java.io.IOException;

public interface PredictionService {
    /**
     * 调取服务获得跌倒检测结果
     * @param imageUrl
     * @return
     */
    PoseDetectionResponse getPrediction(String imageUrl);

    /**
     * 判断并上传跌倒检测报警信息
     * @param imageUrl
     * @param deviceId
     * @return
     * @throws IOException
     */
    RealTimeImage uploadFallInfo(String imageUrl, String deviceId) throws IOException;
}
