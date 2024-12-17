package com.example.edgeserver.scheduler;

import com.example.edgeserver.modules.stickman.model.domain.RealTimeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.edgeserver.util.camera.getPicture;
import org.springframework.stereotype.Service;
import com.example.edgeserver.service.PredictionService;

@Service
public class checkPictureFromCamera {
    @Autowired
    private getPicture getPicture;
    @Autowired
    private PredictionService predictionService;

    @Scheduled(fixedRateString = "${Camera.timeStep}")
    public void scheduleTask() throws Exception {
        System.out.println(Thread.currentThread().getName() + "<>fixedRateTest1 : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
        // 首先获得原图的url
        String imageUrl = getPicture.testGetPictureByUrl();
        System.out.println("checkPictureFromCamera mac: "+imageUrl);
        // 根据url获得deviceId
        String deviceId = "deviceId";
        // 进行判断和上传
        RealTimeImage realTimeImage = predictionService.uploadFallInfo(imageUrl, deviceId);

    }
}
