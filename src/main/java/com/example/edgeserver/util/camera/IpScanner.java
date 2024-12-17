package com.example.edgeserver.util.camera;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IpScanner {

    private volatile boolean found = false;  // 共享标志，表示是否找到目标MAC地址
    private String foundIp = null;  // 保存找到的IP
    private AtomicInteger scannedCount = new AtomicInteger(0);  // 已扫描的IP数量
    private int TOTAL_IPS = 254;
    static String Mac;
    public IpScanner(String Mac) {
        this.Mac=Mac;
    }


    public String scan() {
        String targetMac = Mac;  // 目标MAC地址
        String subnet = "192.168.137";  // 共享热点的子网地址

        // 创建一个线程池来并行扫描多个IP地址
        ExecutorService executorService = Executors.newFixedThreadPool(10);  // 使用10个线程

        // Future 用于获取任务结果，提前停止线程池
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 1; i <= TOTAL_IPS; i++) {
            String ip = subnet + "." + i;
            futures.add(executorService.submit(() -> {
                if (!found) {
                    scanIp(ip, targetMac);
                    // 更新已扫描数量
                    scannedCount.incrementAndGet();
                }
                return null;  // 任务无返回值
            }));
        }

        // 等待所有任务完成
        try {
            for (Future<Void> future : futures) {
                future.get();  // 等待每个任务完成
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executorService.shutdown();  // 关闭线程池

        // 如果找到了目标MAC地址，打印扫描到的IP并传递到ResultHandler
        if (found) {
            System.out.println("Found camera at IP: " + foundIp);
        } else {
            System.out.println("Camera not found.");
        }
        return foundIp;
    }
    // 扫描单个IP
    public void scanIp(String ip, String targetMac) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            System.out.println("Scanning IP: " + ip);  // 添加调试输出，查看扫描的IP

            // 增加较短的超时时间来快速判断 IP 是否可达
            if (address.isReachable(500)) {  // 500ms 超时
                String mac = getMacAddress(ip);
                if (mac != null && mac.equalsIgnoreCase(targetMac)) {
                    synchronized (IpScanner.class) {
                        if (!found) {  // 检查是否已经找到
                            found = true;
                            foundIp = ip;  // 保存找到的IP
                            System.out.println("Found camera at IP: " + ip);  // 输出找到的IP
                        }
                    }
                } else {
                    System.out.println("MAC address for " + ip + " is: " + mac);  // 添加调试信息
                }
            } else {
                System.out.println(ip + " is not reachable.");  // 输出不可达的IP
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取指定IP的MAC地址
    public static String getMacAddress(String ip) {
        String macAddress = null;
        try {
            // 执行 arp 命令
            Process process = Runtime.getRuntime().exec("arp -a " + ip);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // 查找包含目标IP的行
                if (line.contains(ip)) {
                    // 去除多余的空格并分割字符串
                    String[] tokens = line.trim().split("\\s+");

                    // 确保 tokens 数组长度大于等于 2
                    if (tokens.length >= 2) {
                        macAddress = tokens[1];  // MAC 地址是第二个元素
                    }

                    // 检查 MAC 地址是否符合规范
                    if (macAddress != null) {
                        macAddress = macAddress.replace("-", ":");  // 统一格式为 `:` 分隔
                    }

                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macAddress;
    }
}
