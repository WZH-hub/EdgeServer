package com.example.edgeserver.modules.stickman.service.impl;

import com.example.edgeserver.util.StickmanImageProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.edgeserver.util.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpEntity;

@Service
@Slf4j
public class AsyncImageService {

    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH");

    /**
     * Resize and save the image from byte[].
     *
     * @param imgBytes 图像数据
     * @param deviceId 设备ID
     * @param timestamp 时间戳（标准格式）
     * @param baseDirectory 保存图像的根目录路径
     * @return 图像保存的完整路径
     * @throws IOException 如果发生IO错误
     */
    public String uploadImage(byte[] imgBytes, String deviceId, LocalDateTime timestamp, String baseDirectory) throws IOException {
        return processAndSaveImageFromBytes(imgBytes, deviceId, timestamp, baseDirectory);
    }

    /**
     * Save the image from HttpEntity.
     *
     * @param entity HttpEntity 包含图像流
     * @param deviceId 设备ID
     * @param timestamp 时间戳（标准格式）
     * @param baseDirectory 保存图像的根目录路径
     * @return 图像保存的完整路径
     * @throws IOException 如果发生IO错误
     */
    public String uploadImage(HttpEntity entity, String deviceId, LocalDateTime timestamp, String baseDirectory) throws IOException {
        return processAndSaveImageFromEntity(entity, deviceId, timestamp, baseDirectory);
    }

    /**
     * 从字节数组处理并保存图像
     */
    private String processAndSaveImageFromBytes(byte[] imgBytes, String deviceId, LocalDateTime timestamp, String baseDirectory) throws IOException {
        return processAndSaveImage(() -> new ByteArrayInputStream(imgBytes), deviceId, timestamp, baseDirectory);
    }

    /**
     * 从HttpEntity处理并保存图像
     */
    private String processAndSaveImageFromEntity(HttpEntity entity, String deviceId, LocalDateTime timestamp, String baseDirectory) throws IOException {
        return processAndSaveImage(entity::getContent, deviceId, timestamp, baseDirectory);
    }

    /**
     * 统一的图像处理和保存逻辑
     *
     * @param inputStreamSupplier 提供图像输入流的函数
     * @param deviceId            设备ID
     * @param timestamp           时间戳
     * @param baseDirectory       根目录
     * @return 保存路径
     * @throws IOException 如果发生IO错误
     */
    private String processAndSaveImage(InputStreamSupplier inputStreamSupplier, String deviceId, LocalDateTime timestamp, String baseDirectory) throws IOException {
        // 确保目录基础路径存在
        // String baseDirectory = Constants.IMAGE_DIRECTORY;
        ensureDirectoryExists(baseDirectory);

        // 创建日期和小时子目录
        String dateDirectory = baseDirectory + File.separator + DATE_FORMATTER.format(timestamp);
        ensureDirectoryExists(dateDirectory);

        String hourDirectory = dateDirectory + File.separator + HOUR_FORMATTER.format(timestamp);
        ensureDirectoryExists(hourDirectory);

        // 生成文件名
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = deviceId + "_" + formattedTimestamp + ".jpg";

        // 保存文件路径
        Path filePath = Paths.get(hourDirectory, fileName);

        try (InputStream inputStream = inputStreamSupplier.get();
             FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            // 写入图片数据
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            log.info("图像已保存到本地路径: {}", filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("处理图像时发生错误", e);
            throw e;
        }
    }

    /**
     * 确保指定的目录存在，如果不存在则创建
     *
     * @param directoryPath 目录路径
     */
    private void ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("目录已创建: {}", directoryPath);
            } else {
                log.error("无法创建目录: {}", directoryPath);
            }
        }
    }

    @FunctionalInterface
    private interface InputStreamSupplier {
        InputStream get() throws IOException;
    }
}
