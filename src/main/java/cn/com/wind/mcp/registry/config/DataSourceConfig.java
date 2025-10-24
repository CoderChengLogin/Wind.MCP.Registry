package cn.com.wind.mcp.registry.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置类 - 生产环境
 * 配置动态数据源,支持读写分离
 * 仅在非jenkins测试环境下生效
 *
 * @author system
 */
@Slf4j
@Configuration
@org.springframework.context.annotation.Profile("!jenkins")
public class DataSourceConfig {

    /**
     * 读数据源配置 (mcp_registry)
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.ds-reader")
    public HikariConfig readerHikariConfig() {
        return new HikariConfig();
    }

    /**
     * 写数据源配置 (wind_mcp_server)
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.ds-writer")
    public HikariConfig writerHikariConfig() {
        return new HikariConfig();
    }

    /**
     * 读数据源
     */
    @Bean(name = "readerDataSource")
    public DataSource readerDataSource(HikariConfig readerHikariConfig) {
        log.info("初始化读数据源: {}", readerHikariConfig.getJdbcUrl());
        return new HikariDataSource(readerHikariConfig);
    }

    /**
     * 写数据源
     */
    @Bean(name = "writerDataSource")
    public DataSource writerDataSource(HikariConfig writerHikariConfig) {
        log.info("初始化写数据源: {}", writerHikariConfig.getJdbcUrl());
        return new HikariDataSource(writerHikariConfig);
    }

    /**
     * 动态数据源
     * 根据DynamicDataSourceContextHolder中的key动态切换数据源
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource(DataSource readerDataSource, DataSource writerDataSource) {
        log.info("初始化动态数据源");

        DynamicDataSource dynamicDataSource = new DynamicDataSource();

        // 设置所有数据源
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DynamicDataSourceContextHolder.DS_READER, readerDataSource);
        targetDataSources.put(DynamicDataSourceContextHolder.DS_WRITER, writerDataSource);
        dynamicDataSource.setTargetDataSources(targetDataSources);

        // 设置默认数据源
        dynamicDataSource.setDefaultTargetDataSource(readerDataSource);

        log.info("动态数据源配置完成: ds_reader={}, ds_writer={}",
                readerDataSource, writerDataSource);

        return dynamicDataSource;
    }
}
