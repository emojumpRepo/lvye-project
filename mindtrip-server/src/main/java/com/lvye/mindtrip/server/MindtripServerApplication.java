package com.lvye.mindtrip.server;

import com.github.fppt.jedismock.RedisServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.TimeZone;

/**
 * 项目的启动类
 *
 * 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
 *
 * @author 芋道源码
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${mindtrip.info.base-package}
@SpringBootApplication(scanBasePackages = {"${mindtrip.info.base-package}.server", "${mindtrip.info.base-package}.module"})
public class MindtripServerApplication {

    public static void main(String[] args) {
        // 设置 JVM 默认时区为中国标准时间，确保无论服务器部署在哪个地区都使用统一时区
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

        // 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章

        // 检查是否需要启动 Redis Mock 服务器
        // 可以通过系统属性 -Dmindtrip.redis.mock.enabled=false 来禁用
        boolean enableRedisMock = Boolean.parseBoolean(System.getProperty("mindtrip.redis.mock.enabled", "true"));

        if (enableRedisMock) {
            startRedisMockServer();
        } else {
            System.out.println("Redis Mock Server is disabled. Make sure you have a real Redis server running.");
        }

        SpringApplication.run(MindtripServerApplication.class, args);
//        new SpringApplicationBuilder(MindtripServerApplication.class)
//                .applicationStartup(new BufferingApplicationStartup(20480))
//                .run(args);

        // 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.iocoder.cn/quick-start/ 文章
    }

    /**
     * 启动 Redis Mock 服务器，尝试多个端口以避免冲突
     */
    private static void startRedisMockServer() {
        int[] ports = {6379, 6380, 6381, 6382, 6383}; // 尝试多个端口

        for (int port : ports) {
            try {
                RedisServer redisServer = new RedisServer(port);
                redisServer.start();
                System.out.println("Redis Mock Server started successfully on port: " + port);
                return; // 成功启动，退出方法
            } catch (IOException e) {
                System.out.println("Port " + port + " is already in use, trying next port...");
                if (port == ports[ports.length - 1]) {
                    // 如果所有端口都被占用，给出友好的错误提示
                    System.err.println("Failed to start Redis Mock Server: All ports (6379-6383) are in use.");
                    System.err.println("Solutions:");
                    System.err.println("1. Stop other Redis instances running on these ports");
                    System.err.println("2. Run with -Dmindtrip.redis.mock.enabled=false and use external Redis");
                    System.err.println("3. Use 'taskkill /F /IM redis-server.exe' to stop Redis processes (Windows)");
                    throw new RuntimeException("All Redis ports are in use. Please stop other Redis instances or use external Redis.", e);
                }
            }
        }
    }

}
