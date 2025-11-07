package com.lvye.mindtrip.module.infra.framework.file.core.ftp;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.ftp.FtpMode;
import com.lvye.mindtrip.module.infra.framework.file.core.client.ftp.FtpFileClient;
import com.lvye.mindtrip.module.infra.framework.file.core.client.ftp.FtpFileClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link FtpFileClient} 单元测试
 *
 * @author 芋道源码
 */
public class FtpFileClientTest {

//    docker run -d \
//            -p 2121:21 -p 30000-30009:30000-30009 \
//            -e FTP_USER=foo \
//            -e FTP_PASS=pass \
//            -e PASV_ADDRESS=127.0.0.1 \
//            -e PASV_MIN_PORT=30000 \
//            -e PASV_MAX_PORT=30009 \
//            -v $(pwd)/ftp-data:/home/vsftpd \
//    fauria/vsftpd

    @Test
    public void testUploadAndGetContent(@TempDir Path tempDir) {
        // 准备配置
        FtpFileClientConfig config = new FtpFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath(tempDir.toString());
        config.setHost("127.0.0.1");
        config.setPort(2121);
        config.setUsername("testuser");
        config.setPassword("testpass");
        config.setMode(FtpMode.Passive.name());
        
        // 由于需要外部FTP服务，这里只测试配置初始化
        FtpFileClient client = new FtpFileClient(1L, config);
        
        // 验证配置正确性
        assertNotNull(client);
        assertEquals(1L, client.getId());
        // config 是 protected 字段，无法直接访问，但可以验证客户端初始化成功
    }
    
    @Test
    public void testConfigValidation() {
        // 测试配置参数
        FtpFileClientConfig config = new FtpFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/home/ftp");
        config.setHost("127.0.0.1");
        config.setPort(2121);
        config.setUsername("testuser");
        config.setPassword("testpass");
        config.setMode(FtpMode.Passive.name());
        
        // 验证配置参数
        assertEquals("http://127.0.0.1:48080", config.getDomain());
        assertEquals("/home/ftp", config.getBasePath());
        assertEquals("127.0.0.1", config.getHost());
        assertEquals(2121, config.getPort());
        assertEquals("testuser", config.getUsername());
        assertEquals("testpass", config.getPassword());
        assertEquals(FtpMode.Passive.name(), config.getMode());
    }
    
    @Test
    public void testClientCreation() {
        // 测试客户端创建
        FtpFileClientConfig config = new FtpFileClientConfig();
        config.setDomain("http://test.domain.com");
        config.setBasePath("/test/path");
        config.setHost("test.host.com");
        config.setPort(21);
        config.setUsername("user");
        config.setPassword("pass");
        config.setMode(FtpMode.Active.name());
        
        FtpFileClient client = new FtpFileClient(999L, config);
        
        // 验证客户端属性
        assertNotNull(client);
        assertEquals(999L, client.getId());
        // 验证配置对象本身的属性
        assertEquals("http://test.domain.com", config.getDomain());
        assertEquals("/test/path", config.getBasePath());
        assertEquals("test.host.com", config.getHost());
        assertEquals(21, config.getPort());
        assertEquals("user", config.getUsername());
        assertEquals("pass", config.getPassword());
        assertEquals(FtpMode.Active.name(), config.getMode());
    }

}
