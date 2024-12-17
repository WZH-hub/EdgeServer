package com.example.edgeserver.util.camera;

import jakarta.annotation.PostConstruct;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.edgeserver.modules.stickman.service.impl.AsyncImageService;
import com.example.edgeserver.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class getPicture {
    @Value("${Camera.mac}")
    private String mac;
    @Value("${Camera.username}")
    private String username;
    @Value("${Camera.password}")
    private String password;
    @Value("${Camera.realm}")
    private String realm;
    @Value("${Camera.timeStep}")
    private long timeStep;
    private String ip;
    private String url;
    private String postUrl;
    AsyncImageService asyncImageService = new AsyncImageService();
    private String deviceId;

    @PostConstruct
    public void init() {
        System.out.println("getPicture:"+realm);
        /*
        IpScanner is = new IpScanner(mac);//根据MAC地址获取IP地址
        this.ip = is.scan();
        this.url = "http://" + this.ip + "/digest/frmUserLogin";  // 第一次请求的 URL
        this.postUrl = "http://" + this.ip + "/digest/CaptureV2"; // 第二次请求的 URL

         */
    }

    public String testGetPictureByUrl() {
        return this.mac;
    }

    public String getPictureByUrl() throws Exception {
        try {
            // 第一次请求，获取 401 错误并提取 WWW-Authenticate 头部信息
            HttpResponse getResponse = sendGetRequest(this.url);
            if (getResponse.getStatusLine().getStatusCode() == 401) {
                String wwwAuthenticateHeader = getResponse.getFirstHeader("WWW-Authenticate").getValue();
                System.out.println("WWW-Authenticate: " + wwwAuthenticateHeader);

                // 解析出nonce, opaque 和 qop
                String nonce = extractNonce(wwwAuthenticateHeader);
                String opaque = extractOpaque(wwwAuthenticateHeader);
                String qop = extractQop(wwwAuthenticateHeader);

                // 计算 HA1 和 HA2
                String ha1 = md5(this.username + ":" + this.realm + ":" + this.password);
                String ha2 = md5("POST:/digest/frmUserLogin");

                // 打印 HA1 和 HA2 过程，查看密码是否参与计算
                System.out.println("HA1 (using password): " + ha1);
                System.out.println("HA2: " + ha2);

                // 生成 cnonce 和 nonceCount
                String cnonce = generateCnonce();
                String nonceCount = "00000002";  // 假设是第 2 次请求

                // 计算 response
                String response = md5(ha1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + ha2);
                System.out.println("Calculated response: " + response);  // 打印出计算出来的 response

                // 进入无限循环，每隔2秒发送一次POST请求获取图片

                // 第二次请求，带上计算好的 Authorization 头
                HttpResponse postResponse = sendPostRequest(this.postUrl, this.username, nonce, nonceCount, cnonce, qop, opaque, response);
                if (postResponse.getStatusLine().getStatusCode() == 200) {
                    // 获取响应体的 HttpEntity 对象
                    HttpEntity entity = postResponse.getEntity();

                    // 获取响应头中的 Content-Type，以判断是否为图片
                    String contentType = postResponse.getFirstHeader("Content-Type").getValue();
                    System.out.println("Content-Type: " + contentType);

                    // 判断是否是图片类型
                    if (contentType.contains("image")) {
                        // 保存原始图片数据
                        // saveImageFromBinary(entity);  // 直接保存二进制图片
                        String imageUrl = asyncImageService.uploadImage(entity,deviceId,LocalDateTime.now(),Constants.IMAGE_BASE_DIRECTORY);
                        return imageUrl;
                    } else {
                        // 读取并打印文本数据
                        String responseBody = EntityUtils.toString(entity);
                        System.out.println("POST Response: " + responseBody);
                        return null;
                    }
                } else {
                    System.out.println("POST Request failed with status: " + postResponse.getStatusLine().getStatusCode());
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred in getPictureByUrl: " + e.getMessage());
        }
        return null;
    }

    // 发送 GET 请求
    private static HttpResponse sendGetRequest(String url) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("User-Agent", "curl/4.7.1");
        getRequest.setHeader("Content-Type", "application/json");

        return client.execute(getRequest);
    }

    // 发送带有 Digest Authentication 的 POST 请求
    private static HttpResponse sendPostRequest(String url, String username, String nonce, String nonceCount,
                                                String cnonce, String qop, String opaque, String response) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(url);

        // 构建 Authorization 头
        String authorizationHeader = buildDigestAuthHeader(username, nonce, nonceCount, cnonce, qop, opaque, response);
        postRequest.setHeader("Authorization", authorizationHeader);
        postRequest.setHeader("User-Agent", "curl/4.7.1");
        postRequest.setHeader("Content-Type", "application/json; charset=utf-8");

        // 设置请求体
        String jsonBody = "{\n" +
                "  \"Type\": 1,\n" +
                "  \"Dev\": 1,\n" +
                "  \"Ch\": 1,\n" +
                "  \"Data\": {\n" +
                "    \"DataType\": 0,\n" +           //DataType 0为图片为二进制数据，为1则图片为64Base格式输出
                "    \"StreamNo\": 1\n" +
                "  }\n" +
                "}";

        StringEntity entity = new StringEntity(jsonBody);
        postRequest.setEntity(entity);

        return client.execute(postRequest);
    }

    // 构建 Digest 认证头
    private static String buildDigestAuthHeader(String username, String nonce, String nonceCount,
                                                String cnonce, String qop, String opaque, String response) {
        String authHeader = "Digest username=\"" + username + "\", realm=\"webserver\", nonce=\"" + nonce + "\", "
                + "uri=\"/digest/frmUserLogin\", response=\"" + response + "\", cnonce=\"" + cnonce + "\", "
                + "opaque=\"" + opaque + "\", qop=" + qop + ", nc=" + nonceCount;
        System.out.println("Authorization Header: " + authHeader);  // 打印头部信息
        return authHeader;
    }

    // MD5 计算
    private static String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 提取 nonce
    private static String extractNonce(String header) {
        String[] parts = header.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("nonce")) {
                return part.split("=")[1].replaceAll("\"", "").trim();
            }
        }
        return null;
    }

    // 提取 opaque
    private static String extractOpaque(String header) {
        String[] parts = header.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("opaque")) {
                return part.split("=")[1].replaceAll("\"", "").trim();
            }
        }
        return null;
    }

    // 提取 qop
    private static String extractQop(String header) {
        String[] parts = header.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("qop")) {
                return part.split("=")[1].replaceAll("\"", "").trim();
            }
        }
        return null;
    }

    // 生成随机的 cnonce (客户端 nonce)
    private static String generateCnonce() {
        return Integer.toHexString((int) (Math.random() * 0x100000000L));
    }

    // 保存图片二进制数据到文件
    private static void saveImageFromBinary(HttpEntity entity) throws IOException {
        // 获取响应的输入流
        InputStream inputStream = entity.getContent();

        // 创建目录，确保路径存在
        File directory = new File("image");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 创建文件输出流，保存图片到指定路径
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDate = now.format(formatter);

        // 创建带有时间戳的文件名
        String fileName = "output_image_" + formattedDate + ".jpg";

        // 创建文件对象
        File outputFile = new File(directory, fileName);

        // 创建输出流
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[1024];
        int bytesRead;
        // 写入图片数据
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // 关闭流
        inputStream.close();
        outputStream.close();

        System.out.println("Image saved successfully to: " + outputFile.getAbsolutePath());
    }



}
