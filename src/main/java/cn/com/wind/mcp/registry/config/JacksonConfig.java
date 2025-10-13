package cn.com.wind.mcp.registry.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson配置类
 * <p>
 * 配置JSON序列化时不转义中文字符，确保中文正常显示
 * </p>
 *
 * @author system
 * @date 2025-10-12
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper，禁用非ASCII字符转义
     * <p>
     * 默认情况下，Jackson会将非ASCII字符（如中文）转义为Unicode格式
     * 通过禁用ESCAPE_NON_ASCII特性，可以直接输出UTF-8字符
     * </p>
     *
     * @param builder Jackson2ObjectMapperBuilder
     * @return 配置好的ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 禁用非ASCII字符转义，直接输出UTF-8编码的中文
        objectMapper.getFactory().disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        return objectMapper;
    }
}
