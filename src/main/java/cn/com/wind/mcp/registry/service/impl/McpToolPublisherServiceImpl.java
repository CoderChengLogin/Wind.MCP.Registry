package cn.com.wind.mcp.registry.service.impl;

import cn.com.wind.mcp.registry.annotation.TargetDataSource;
import cn.com.wind.mcp.registry.dto.McpToolPublishDto;
import cn.com.wind.mcp.registry.service.McpToolPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MCP工具发布服务实现类
 * 核心业务逻辑：将工具数据从mcp_registry库发布到wind_mcp_server库
 *
 * @author system
 */
@Slf4j
@Service
public class McpToolPublisherServiceImpl implements McpToolPublisherService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 发布工具到目标数据库
     * 使用@TargetDataSource注解切换到写数据源 (ds_writer)
     *
     * @param publishDto 发布数据
     * @throws Exception 发布失败异常
     */
    @Override
    @TargetDataSource("ds_writer")
    @Transactional(rollbackFor = Exception.class)
    public void publishTool(McpToolPublishDto publishDto) throws Exception {
        log.info("开始发布工具: toolName={}, toolNum={}", publishDto.getToolName(), publishDto.getToolNum());

        try {
            // 1. 验证必填字段
            if (publishDto.getToolName() == null || publishDto.getToolName().trim().isEmpty()) {
                throw new Exception("工具名称(toolName)不能为空");
            }
            if (publishDto.getToolNum() == null) {
                throw new Exception("工具编号(toolNum)不能为空");
            }
            if (publishDto.getToolVersion() == null) {
                throw new Exception("工具版本(toolVersion)不能为空");
            }

            // 2. 检查工具是否已存在
            String checkSql = "SELECT COUNT(*) FROM mcp_tool WHERE tool_num = ? AND tool_version = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class,
                    publishDto.getToolNum(), publishDto.getToolVersion());

            if (count != null && count > 0) {
                log.warn("工具已存在: toolNum={}, version={}", publishDto.getToolNum(), publishDto.getToolVersion());
                throw new Exception("工具已发布,无法重复发布");
            }

            // 3. 插入MCP工具配置
            insertMcpToolConfig(publishDto);

            // 3. 根据转换类型插入对应的原始工具和模板转换器
            String convertType = publishDto.getConvertType();
            if ("1".equals(convertType)) {
                // HTTP类型
                insertHttpToolAndConverter(publishDto);
            } else if ("2".equals(convertType)) {
                // Expo类型
                insertExpoToolAndConverter(publishDto);
            }

            log.info("工具发布成功: toolName={}", publishDto.getToolName());

        } catch (Exception e) {
            log.error("工具发布失败: toolName={}, error={}", publishDto.getToolName(), e.getMessage(), e);
            throw new Exception("发布失败: " + e.getMessage(), e);
        }
    }

    /**
     * 插入MCP工具配置
     */
    private void insertMcpToolConfig(McpToolPublishDto dto) {
        String sql = "INSERT INTO mcp_tool (" +
                "tool_num, tool_version, valid, tool_name, tool_description, " +
                "name_display, description_display, input_schema, output_schema, " +
                "stream_output, convert_type, tool_type, " +
                "create_time, create_by, update_time, update_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DATE_FORMATTER);

        jdbcTemplate.update(sql,
                dto.getToolNum(),
                dto.getToolVersion(),
                dto.getValid(),
                dto.getToolName(),
                dto.getToolDescription(),
                dto.getNameDisplay(),
                dto.getDescriptionDisplay(),
                dto.getInputSchema(),
                dto.getOutputSchema(),
                dto.getStreamOutput(),
                dto.getConvertType(),
                dto.getToolType(),
//                dto.getProviderId(),
                nowStr,
                "registry",
                nowStr,
                "registry"
        );

        log.warn("插入MCP工具配置成功: toolNum={}", dto.getToolNum());
    }

    /**
     * 插入HTTP工具和转换器
     */
    private void insertHttpToolAndConverter(McpToolPublishDto dto) {
        // 插入原始HTTP工具
        if (dto.getHttpReqUrl() != null) {
            String httpSql = "INSERT INTO origin_tool_http (" +
                    "provider_tool_num, req_url, req_method, req_headers, " +
                    "input_schema, output_schema, " +
                    "create_time, create_by, update_time, update_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?,  ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DATE_FORMATTER);

            jdbcTemplate.update(httpSql,
                    dto.getToolNum(),
                    dto.getHttpReqUrl(),
                    dto.getHttpReqMethod(),
                    dto.getHttpReqHeaders(),
                    dto.getHttpInputSchema(),
                    dto.getHttpOutputSchema(),
//                    dto.getProviderId(),
                    nowStr,
                    "registry",
                    nowStr,
                    "registry"
            );

            log.warn("插入HTTP工具配置成功: toolNum={}", dto.getToolNum());
        }

        // 插入HTTP模板转换器
        if (dto.getHttpTemplateReqUrl() != null) {
            String templateSql = "INSERT INTO http_template_converter (" +
                    "tool_num, tool_version, req_url, req_method, req_headers, " +
                    "req_body, resp_body, provider_tool_num, " +
                    "create_time, create_by, update_time, update_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DATE_FORMATTER);

            jdbcTemplate.update(templateSql,
                    dto.getToolNum(),
                    dto.getToolVersion(),
                    dto.getHttpTemplateReqUrl(),
                    dto.getHttpTemplateReqMethod(),
                    dto.getHttpTemplateReqHeaders(),
                    dto.getHttpTemplateReqBody(),
                    dto.getHttpTemplateRespBody(),
                    dto.getToolNum(),
                    nowStr,
                    "registry",
                    nowStr,
                    "registry"
            );

            log.warn("插入HTTP模板转换器配置成功: toolNum={}", dto.getToolNum());
        }
    }

    /**
     * 插入Expo工具和转换器
     */
    private void insertExpoToolAndConverter(McpToolPublishDto dto) {
        // 插入原始Expo工具
        if (dto.getExpoAppClass() != null) {
            String expoSql = "INSERT INTO origin_tool_expo (" +
                    "provider_tool_num, app_class, command_id, function_name, " +
                    "expo_api_define, " +
                    "create_time, create_by, update_time, update_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DATE_FORMATTER);

            jdbcTemplate.update(expoSql,
                    dto.getToolNum(),
                    dto.getExpoAppClass(),
                    dto.getExpoCommandId(),
                    dto.getExpoFunctionName(),
                    dto.getExpoApiDefine(),
//                    dto.getProviderId(),
                    nowStr,
                    "registry",
                    nowStr,
                    "registry"
            );

            log.warn("插入Expo工具配置成功: toolNum={}", dto.getToolNum());
        }

        // 插入Expo模板转换器
        if (dto.getExpoTemplateAppClass() != null) {
            String templateSql = "INSERT INTO expo_template_converter (" +
                    "tool_num, tool_version, app_class, command_id, " +
                    "input_args, output_args, provider_tool_num, " +
                    "create_time, create_by, update_time, update_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DATE_FORMATTER);

            jdbcTemplate.update(templateSql,
                    dto.getToolNum(),
                    dto.getToolVersion(),
                    dto.getExpoTemplateAppClass(),
                    dto.getExpoTemplateCommandId(),
                    dto.getExpoTemplateInputArgs(),
                    dto.getExpoTemplateOutputArgs(),
                    dto.getToolNum(),
                    nowStr,
                    "registry",
                    nowStr,
                    "registry"
            );

            log.warn("插入Expo模板转换器配置成功: toolNum={}", dto.getToolNum());
        }
    }

    /**
     * 检查并初始化目标数据库
     */
    @Override
    @TargetDataSource("ds_writer")
    public void initializeTargetDatabase() throws Exception {
        log.info("检查并初始化目标数据库: wind_mcp_server");

        try {
            // 1. 检查数据库是否存在
            String checkDbSql = "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'wind_mcp_server'";
            String dbName = jdbcTemplate.queryForObject(checkDbSql, String.class);

            if (dbName == null) {
                log.info("目标数据库不存在，开始创建...");
                // 创建数据库
//                jdbcTemplate.execute("CREATE DATABASE wind_mcp_server CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
//                log.info("目标数据库创建成功");

                // 2. 切换到目标数据库并创建必要的表
//                jdbcTemplate.execute("USE wind_mcp_server");
//                createTablesIfNotExist();
//
//                log.info("目标数据库初始化完成");

                throw new RuntimeException("目标数据库不存在");
            }
        } catch (Exception e) {
            log.error("初始化目标数据库失败: {}", e.getMessage(), e);
            throw new Exception("初始化数据库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建必要的表（如果不存在）
     */
    private void createTablesIfNotExist() {
        // 创建mcp_tool表
        String mcpToolTableSql = "CREATE TABLE IF NOT EXISTS mcp_tool (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "tool_num BIGINT NOT NULL, " +
                "tool_version BIGINT NOT NULL, " +
                "valid VARCHAR(10), " +
                "tool_name VARCHAR(255) NOT NULL, " +
                "tool_description TEXT, " +
                "name_display TEXT, " +
                "description_display TEXT, " +
                "input_schema TEXT, " +
                "output_schema TEXT, " +
                "stream_output VARCHAR(10), " +
                "convert_type VARCHAR(10), " +
                "tool_type VARCHAR(10), " +
                "provider_id BIGINT, " +
                "create_time DATETIME, " +
                "create_by VARCHAR(100), " +
                "update_time DATETIME, " +
                "update_by VARCHAR(100), " +
                "UNIQUE KEY uk_tool_num_version (tool_num, tool_version)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(mcpToolTableSql);

        // 创建origin_tool_http表
        String httpToolTableSql = "CREATE TABLE IF NOT EXISTS origin_tool_http (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "provider_tool_num BIGINT NOT NULL UNIQUE, " +
                "req_url VARCHAR(500), " +
                "req_method VARCHAR(20), " +
                "req_headers TEXT, " +
                "input_schema TEXT, " +
                "output_schema TEXT, " +
                "provider_id BIGINT, " +
                "create_time DATETIME, " +
                "create_by VARCHAR(100), " +
                "update_time DATETIME, " +
                "update_by VARCHAR(100)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(httpToolTableSql);

        // 创建http_template_converter表
        String httpTemplateTableSql = "CREATE TABLE IF NOT EXISTS http_template_converter (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "tool_num BIGINT NOT NULL, " +
                "tool_version BIGINT NOT NULL, " +
                "req_url VARCHAR(500), " +
                "req_method VARCHAR(20), " +
                "req_headers TEXT, " +
                "req_body TEXT, " +
                "resp_body TEXT, " +
                "provider_tool_num BIGINT, " +
                "create_time DATETIME, " +
                "create_by VARCHAR(100), " +
                "update_time DATETIME, " +
                "update_by VARCHAR(100)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(httpTemplateTableSql);

        // 创建origin_tool_expo表
        String expoToolTableSql = "CREATE TABLE IF NOT EXISTS origin_tool_expo (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "provider_tool_num BIGINT NOT NULL UNIQUE, " +
                "app_class INT, " +
                "command_id INT, " +
                "function_name VARCHAR(255), " +
                "expo_api_define TEXT, " +
                "provider_id BIGINT, " +
                "create_time DATETIME, " +
                "create_by VARCHAR(100), " +
                "update_time DATETIME, " +
                "update_by VARCHAR(100)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(expoToolTableSql);

        // 创建expo_template_converter表
        String expoTemplateTableSql = "CREATE TABLE IF NOT EXISTS expo_template_converter (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "tool_num BIGINT NOT NULL, " +
                "tool_version BIGINT NOT NULL, " +
                "app_class INT, " +
                "command_id INT, " +
                "input_args TEXT, " +
                "output_args TEXT, " +
                "provider_tool_num BIGINT, " +
                "create_time DATETIME, " +
                "create_by VARCHAR(100), " +
                "update_time DATETIME, " +
                "update_by VARCHAR(100)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(expoTemplateTableSql);

        log.info("所有必要的表已创建或已存在");
    }
}
