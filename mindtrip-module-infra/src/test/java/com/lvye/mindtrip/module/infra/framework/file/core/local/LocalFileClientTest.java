package com.lvye.mindtrip.module.infra.framework.file.core.local;

import cn.hutool.core.util.IdUtil;
import com.lvye.mindtrip.module.infra.framework.file.core.client.local.LocalFileClient;
import com.lvye.mindtrip.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static com.lvye.mindtrip.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

public class LocalFileClientTest {

    @Test
    public void testUploadAndDelete(@TempDir Path tempDir) {
        // 创建客户端
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath(tempDir.toString());
        LocalFileClient client = new LocalFileClient(1L, config);
        client.init();
        
        // 准备测试数据
        String fileName = IdUtil.fastSimpleUUID() + ".txt";
        byte[] testContent = "This is test content for file upload".getBytes(StandardCharsets.UTF_8);
        
        // 测试上传文件
        String uploadUrl = client.upload(testContent, fileName, "text/plain");
        assertNotNull(uploadUrl, "上传应该返回文件URL");
        assertTrue(uploadUrl.contains(fileName), "返回的URL应该包含文件名");
        
        // 测试获取文件内容
        byte[] downloadedContent = client.getContent(fileName);
        assertNotNull(downloadedContent, "应该能够获取上传的文件内容");
        assertArrayEquals(testContent, downloadedContent, "下载的内容应该与上传的内容一致");
        
        // 测试删除文件
        client.delete(fileName);
        
        // 验证文件已被删除
        byte[] deletedContent = client.getContent(fileName);
        assertNull(deletedContent, "删除后应该无法获取文件内容");
    }

    @Test
    public void testGetContent_notFound(@TempDir Path tempDir) {
        // 创建客户端
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath(tempDir.toString());
        LocalFileClient client = new LocalFileClient(2L, config);
        client.init();
        
        // 测试获取不存在的文件
        String nonExistentFileName = randomString() + ".txt";
        byte[] content = client.getContent(nonExistentFileName);
        
        // 验证结果
        assertNull(content, "获取不存在的文件应该返回null");
    }

}
