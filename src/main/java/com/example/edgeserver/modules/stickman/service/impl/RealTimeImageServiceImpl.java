package com.example.edgeserver.modules.stickman.service.impl;

import com.example.edgeserver.entity.R;
import com.example.edgeserver.modules.stickman.model.domain.RealTimeImage;
import com.example.edgeserver.modules.stickman.service.RealTimeImageService;
import com.example.edgeserver.util.Constants;
import com.example.edgeserver.util.StickmanImageProcessUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class RealTimeImageServiceImpl implements RealTimeImageService {

    @Autowired
    private AsyncImageService imageUploadService;

    @Override
    public RealTimeImage combineStickmanAndBackground(String deviceId, String keyPoints, String imageUrl) {
        log.info("接收到设备id: {} 的关键点数据", deviceId);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<List<List<Float>>> keyPointList = objectMapper.readValue(keyPoints, new TypeReference<>() {
            });

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endTime = now.withMinute(59).withSecond(59).withNano(999999999);

            RealTimeImage realTimeImage = new RealTimeImage();
            // 原图
            realTimeImage.setImageUrl(imageUrl);
            // 火柴人+原图
            byte[] combinedByte = StickmanImageProcessUtils.drawStickManInBackground(keyPointList, imageUrl);
            String combinedUrl = imageUploadService.uploadImage(combinedByte, deviceId,LocalDateTime.now(), Constants.IMAGE_COMBINED_DIRECTORY);
            realTimeImage.setCombinedImageUrl(combinedUrl);
            // 火柴人+背景图
            // 还没有理解背景图是如何获得的，暂且空着
            // https://github.com/hfqsjk/qiaoshi-system/blob/stickman_login_wrt/src/main/java/com/qiaoshi/qiaoshisystem/modules/stickman/service/impl/RealTimeImageServiceImpl.java
            realTimeImage.setStickmanImageUrl(combinedUrl);

            return realTimeImage;



        } catch (Exception e) {
            log.error("处理关键点数据时发生错误", e);
            return null;
        }

    }
}
