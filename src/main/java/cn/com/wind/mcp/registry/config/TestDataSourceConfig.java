package cn.com.wind.mcp.registry.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试环境数据源配置类
 * 使用H2内存数据库模拟动态数据源(读写分离)
 * 仅在jenkins测试环境下生效
 *
 * @author system
 */
@Slf4j
@Configuration
@Profile("jenkins")
public class TestDataSourceConfig {

    /**
     * 测试环境读数据源配置 (H2内存数据库)
     *
     * @return HikariConfig配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.ds-reader")
    public HikariConfig testReaderHikariConfig() {
        return new HikariConfig();
    }

    /**
     * 测试环境写数据源配置 (H2内存数据库,与读数据源共享)
     *
     * @return HikariConfig配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.ds-writer")
    public HikariConfig testWriterHikariConfig() {
        return new HikariConfig();
    }

    /**
     * 测试环境读数据源
     * 使用H2内存数据库
     *
     * @param testReaderHikariConfig 读数据源配置
     * @return 数据源对象
     */
    @Bean(name = "readerDataSource")
    public DataSource testReaderDataSource(HikariConfig testReaderHikariConfig) {
        log.info("初始化测试环境读数据源(H2): {}", testReaderHikariConfig.getJdbcUrl());
        return new HikariDataSource(testReaderHikariConfig);
    }

    /**
     * 测试环境写数据源
     * 使用H2内存数据库(与读数据源共享同一个数据库)
     *
     * @param testWriterHikariConfig 写数据源配置
     * @return 数据源对象
     */
    @Bean(name = "writerDataSource")
    public DataSource testWriterDataSource(HikariConfig testWriterHikariConfig) {
        log.info("初始化测试环境写数据源(H2): {}", testWriterHikariConfig.getJdbcUrl());
        return new HikariDataSource(testWriterHikariConfig);
    }

    /**
     * 测试环境动态数据源
     * 配置读写数据源,实际都指向同一个H2内存数据库
     *
     * @param readerDataSource 读数据源
     * @param writerDataSource 写数据源
     * @return 动态数据源对象
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource(DataSource readerDataSource, DataSource writerDataSource) {
        log.info("初始化测试环境动态数据源");

        DynamicDataSource dynamicDataSource = new DynamicDataSource();

        // 设置所有数据源(测试环境中读写都指向H2)
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DynamicDataSourceContextHolder.DS_READER, readerDataSource);
        targetDataSources.put(DynamicDataSourceContextHolder.DS_WRITER, writerDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);

        // 设置默认数据源
        dynamicDataSource.setDefaultTargetDataSource(readerDataSource);

        log.info("测试环境动态数据源配置完成: ds_reader(H2)={}, ds_writer(H2)={}",
                readerDataSource, writerDataSource);

        return dynamicDataSource;
    }
}
