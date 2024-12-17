package com.example.edgeserver.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@Slf4j
public class StickmanImageProcessUtils {
    static final int ORIGIN_X = 0;

    static final int ORIGIN_Y = 0;
    // 人体关键点数量
    static final int NUM_OF_KEY_POINTS = 17;

    // 椭圆关键点的高
    static final int POINT_HEIGHT = 10;

    // 椭圆关键点的宽
    static final int POINT_WIDTH = 10;

    static final int LINE_WIDTH = 4;

    static final String FORMAT_NAME = "jpg";

    static final int targetWidth = 640;

    static final int targetHeight = 360;

    static final int[][] SKELETON = {
            {15, 13}, {13, 11}, {16, 14}, {14, 12}, {11, 12},
            {5, 11}, {6, 12}, {5, 6}, {5, 7}, {6, 8},
            {7, 9}, {8, 10}, {1, 2}, {0, 1}, {0, 2},
            {1, 3}, {2, 4}, {3, 5}, {4, 6}
    };
    // 图像尺寸
    static final int DISPLAY_WIDTH = 800;
    static final int DISPLAY_HEIGHT = 480;
    static final int RGB_888_WIDTH = 1920;
    static final int RGB_888_HEIGHT = 1080;
    // 骨骼颜色
    static final Color[] SKELETON_COLORS = {
            new Color(100, 149, 237), new Color(255, 105, 180), new Color(60, 179, 113),
            new Color(255, 165, 0), new Color(255, 20, 147), new Color(147, 112, 219),
            new Color(255, 140, 0), new Color(30, 144, 255), new Color(50, 205, 50),
            new Color(255, 69, 0), new Color(138, 43, 226), new Color(255, 20, 147),
            new Color(218, 165, 32), new Color(255, 140, 0), new Color(255, 0, 255),
            new Color(75, 0, 130), new Color(255, 99, 71), new Color(127, 255, 0),
            new Color(255, 255, 255)
    };

    // 关键点颜色
    static final Color[] POINT_COLORS = {
            new Color(0, 128, 255), new Color(255, 0, 0), new Color(255, 140, 0),
            new Color(0, 255, 0), new Color(255, 0, 255), new Color(255, 255, 0),
            new Color(0, 255, 255), new Color(255, 192, 203), new Color(240, 230, 140),
            new Color(144, 238, 144), new Color(238, 130, 238), new Color(255, 99, 71),
            new Color(240, 128, 128), new Color(221, 160, 221), new Color(135, 206, 250),
            new Color(0, 0, 255), new Color(255, 215, 0)
    };

    /**
     * 根据关键点画火柴人，背景为白色
     *
     * @param keyPointList 关键点信息
     * @return 图片字节数组
     * @throws IOException 异常
     */
    public static byte[] drawStickManDefault(List<List<List<Float>>> keyPointList) throws IOException {
        BufferedImage img = new BufferedImage(DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = img.createGraphics();
        graphics2D.setColor(java.awt.Color.WHITE);
        graphics2D.fillRect(ORIGIN_X, ORIGIN_Y, DISPLAY_WIDTH, DISPLAY_HEIGHT);
        return jointPointAndSkeleton(keyPointList, graphics2D, img);
    }

    public static byte[] drawStickManInBackground(List<List<List<Float>>> keyPointList, String backgroundURL) throws IOException, RuntimeException {
        BufferedImage backgroundImage;
        if (backgroundURL == null || backgroundURL.isEmpty()) {
            // 创建一个白色的背景图像
            backgroundImage = new BufferedImage(DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = backgroundImage.createGraphics();
            graphics2D.setColor(Color.WHITE);
            graphics2D.fillRect(ORIGIN_X, ORIGIN_Y, DISPLAY_WIDTH, DISPLAY_HEIGHT);
            graphics2D.dispose();
        } else {
            try {
                // backgroundImage = ImageIO.read(new URL(backgroundURL));
                // 修改为从本地路径读取图片
                File backgroundFile = new File(backgroundURL);  // 创建一个 File 对象
                backgroundImage = ImageIO.read(backgroundFile); // 通过 ImageIO 读取本地文件
            } catch (IOException e) {
                throw new RuntimeException("Failed to get image from URL: " + backgroundURL, e);
            }
        }

        // 创建图像并绘制背景
        BufferedImage jointImg = new BufferedImage(DISPLAY_WIDTH, DISPLAY_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = jointImg.createGraphics();
        graphics2D.drawImage(backgroundImage, ORIGIN_X, ORIGIN_Y, DISPLAY_WIDTH, DISPLAY_HEIGHT, null);

        return jointPointAndSkeleton(keyPointList, graphics2D, jointImg);
    }

    private static byte[] jointPointAndSkeleton(List<List<List<Float>>> keyPointList, Graphics2D graphics2D, BufferedImage img) throws IOException {
        // 绘制关键点和骨骼
        for (List<List<Float>> oneManKeyPoints : keyPointList) {
            for (int k = 0; k < NUM_OF_KEY_POINTS; k++) {
                float originX = oneManKeyPoints.get(k).get(0);
                float originY = oneManKeyPoints.get(k).get(1);

                int scaledX = scaleX(originX);
                int scaledY = scaleY(originY);

                graphics2D.setColor(POINT_COLORS[k]);
                graphics2D.fillOval(scaledX, scaledY, POINT_WIDTH, POINT_HEIGHT);
            }

            for (int k = 0; k < SKELETON.length; k++) {
                int[] ske = SKELETON[k];

                int pos1X = scaleX(oneManKeyPoints.get(ske[0]).get(0));
                int pos1Y = scaleY(oneManKeyPoints.get(ske[0]).get(1));
                int pos2X = scaleX(oneManKeyPoints.get(ske[1]).get(0));
                int pos2Y = scaleY(oneManKeyPoints.get(ske[1]).get(1));

                graphics2D.setColor(SKELETON_COLORS[k]);
                graphics2D.setStroke(new BasicStroke(LINE_WIDTH));
                graphics2D.drawLine(pos1X, pos1Y, pos2X, pos2Y);
            }
        }

        graphics2D.dispose();
        // 将图像转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, FORMAT_NAME, outputStream);

        return outputStream.toByteArray();
    }
    private static int scaleX(float x) {
        return (int) (x * DISPLAY_WIDTH / RGB_888_WIDTH);
    }

    private static int scaleY(float y) {
        return (int) (y * DISPLAY_HEIGHT / RGB_888_HEIGHT);
    }

    public static byte[] resizeJPG(byte[] imgBytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes);
        BufferedImage img = ImageIO.read(bis);
        // 调整图像大小 (640x360)
        BufferedImage resizedImg = resizeImage(img);
        // 将图像重新编码为 JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImg, "jpg", baos);
        return baos.toByteArray();
    }

    public static BufferedImage resizeImage(BufferedImage originalImage) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        return resizedImage;
    }
}