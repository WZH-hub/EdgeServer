package com.example.edgeserver;

import com.example.edgeserver.entity.PoseDetectionResponse;
import com.example.edgeserver.modules.stickman.service.impl.AsyncImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.edgeserver.util.Constants;
import com.example.edgeserver.service.PredictionService;
import com.example.edgeserver.util.StickmanImageProcessUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EdgeServerApplication {

    @Autowired
    private PredictionService predictionService;


    private static final String url = "C:/Users/WANGZHENHAO/Desktop/photo-3.jpg";

    public static void main(String[] args) throws IOException {
        System.out.println("Test main");
        ConfigurableApplicationContext context = SpringApplication.run(EdgeServerApplication.class, args);
        EdgeServerApplication edgeServerApplication = context.getBean(EdgeServerApplication.class);




        readImageAsBytes();
        // testPrediction(edgeServerApplication);


    }

    public static void testPrediction(EdgeServerApplication edgeServerApplication) throws IOException {
        PoseDetectionResponse response = edgeServerApplication.testService();
        System.out.println(response);
        System.out.println(response.getFall());
        AsyncImageService asyncImageService = new AsyncImageService();
        List<List<List<Float>>> outerList = new ArrayList<>();
        outerList.add(response.getData());
        byte[] combinedByte = StickmanImageProcessUtils.drawStickManInBackground(outerList, url);
        System.out.println(combinedByte);
        String newPath = asyncImageService.uploadImage(combinedByte,"0", LocalDateTime.now(),Constants.IMAGE_BASE_DIRECTORY);
    }

    // 使用注入的服务
    public PoseDetectionResponse testService() {
        if (predictionService != null) {
            return predictionService.getPrediction(url);
        } else {
            System.out.println("predictionService is not injected.");
        }
        return null;
    }

    // 测试图片读取和保存
    public static void readImageAsBytes() throws IOException {
        String imagePath = "C://Users//WANGZHENHAO//Desktop//CBD49F4EF6CB9DE3A2DCFC2F0733F594.jpg";
        Path path = new File(imagePath).toPath();
        byte[] imageBytes = Files.readAllBytes(path);
        System.out.println("成功读取图片，大小为: " + imageBytes.length + " 字节");
        AsyncImageService asyncImageService = new AsyncImageService();
        String newPath = asyncImageService.uploadImage(imageBytes,"0", LocalDateTime.now(),Constants.IMAGE_BASE_DIRECTORY);
        String newPath_2 = asyncImageService.uploadImage(imageBytes,"0", LocalDateTime.now(),Constants.IMAGE_COMBINED_DIRECTORY);
        System.out.println(newPath);
    }



}
