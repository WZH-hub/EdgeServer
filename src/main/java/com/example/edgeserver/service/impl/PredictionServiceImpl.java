package com.example.edgeserver.service.impl;

import com.example.edgeserver.entity.PoseDetectionResponse;
import com.example.edgeserver.modules.stickman.model.domain.RealTimeImage;
import com.example.edgeserver.modules.stickman.service.RealTimeImageService;
import com.example.edgeserver.service.PredictionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PredictionServiceImpl implements PredictionService {

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private RealTimeImageService realTimeImageService;

    /**
     * 调取服务获得跌倒检测结果
     * @param imageUrl
     * @return
     */
    @Override
    public PoseDetectionResponse getPrediction(String imageUrl) {
        String baseUrl = "http://localhost:8000/predict/";
        try {
            // 构建请求
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            // 调用服务并获取响应
            Mono<PoseDetectionResponse> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("image_url", imageUrl).build())
                    .retrieve()
                    .bodyToMono(PoseDetectionResponse.class);

            // 阻塞式获取结果（如果需要异步处理，可返回 Mono）
            return responseMono.block();
        } catch (Exception e) {
            // 异常处理
            System.err.println("图片检测时出错：" + e.getMessage());
        }
        return null;
    }

    /**
     * 判断并上传跌倒检测报警信息
     * @param imageUrl
     * @param deviceId
     * @return
     * @throws IOException
     */
    @Override
    public RealTimeImage uploadFallInfo(String imageUrl, String deviceId) throws IOException {
        String postUrl = "http://121.40.120.244:8080/stickman/warning/frame/";
        // 首先获得对照片的判断结果
        PoseDetectionResponse response = getPrediction(imageUrl);
        try {
            // 发现跌倒信息
            if (response.getFall()) {
                // 将推理结果可视化图片创造出来
                List<List<List<Float>>> data = processKeyPointList(response.getData());
                ObjectMapper objectMapper = new ObjectMapper();
                String keyPoints = objectMapper.writeValueAsString(data);
                // 进行图片拼接
                RealTimeImage realTimeImage = realTimeImageService.combineStickmanAndBackground(deviceId, keyPoints, imageUrl);
                String combinedImageUrl = realTimeImage.getCombinedImageUrl();
                // 转为byte[]
                Path path = new File(combinedImageUrl).toPath();
                byte[] imageBytes = Files.readAllBytes(path);
                // 构建请求
                WebClient webClient = webClientBuilder.baseUrl(postUrl).build();
                // 调用服务并获取响应
                Mono<Void> responseMono = webClient.post()
                        .uri(uriBuilder -> uriBuilder.queryParam("device_id", deviceId).build())
                        .header("Content-Type", "image/jpeg")
                        .header("Content-Length", String.valueOf(imageBytes.length))
                        .bodyValue(imageBytes)
                        .retrieve()
                        .bodyToMono(Void.class);
                // 阻塞式获取结果
                responseMono.block();
                // 输出成功信息
                System.out.println("上传跌倒报警成功");
                return realTimeImage;
            }
        } catch (Exception e) {
            // 异常处理
            System.err.println("连接服务器时出错：" + e.getMessage());
        }
        return null;
    }

    private List<List<List<Float>>> processKeyPointList(List<List<Float>> data) {
        // 将获得的List<List<Float>> data转为合适的List<List<List<Float>>>
        // 等待仇DY的反馈
        List<List<List<Float>>> keyPointList = new ArrayList<>();
        for (List<Float> temp : data) {
            List<List<Float>> tempList = new ArrayList<>();
            tempList.add(temp);
            keyPointList.add(tempList);
        }
        return keyPointList;
    }
}
