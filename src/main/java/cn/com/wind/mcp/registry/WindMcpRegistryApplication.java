package cn.com.wind.mcp.registry;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>
 * Wind MCP Registry 启动器
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-11-08 16:48
 */
@SpringBootApplication
@MapperScan("cn.com.wind.mcp.registry.mapper")
public class WindMcpRegistryApplication {

    /**
     * 主方法，启动Spring Boot应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WindMcpRegistryApplication.class, args);
    }
}