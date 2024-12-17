package com.example.edgeserver.modules.stickman.service;

import com.example.edgeserver.entity.R;
import com.example.edgeserver.modules.stickman.model.domain.RealTimeImage;

public interface RealTimeImageService {
    RealTimeImage combineStickmanAndBackground(String deviceId, String keyPoints, String imageUrl);
}
