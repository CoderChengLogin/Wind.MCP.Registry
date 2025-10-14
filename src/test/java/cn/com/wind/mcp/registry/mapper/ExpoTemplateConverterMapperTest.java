package cn.com.wind.mcp.registry.mapper;

import java.time.LocalDateTime;

import cn.com.wind.mcp.registry.entity.ExpoTemplateConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * ExpoTemplateConverter Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
@ActiveProfiles("jenkins")
public class ExpoTemplateConverterMapperTest {

    @Autowired
    private ExpoTemplateConverterMapper expoTemplateConverterMapper;

    @Test
    void testInsertExpoTemplateConverter() {
        ExpoTemplateConverter converter = new ExpoTemplateConverter();
        converter.setToolNum(12345L);
        converter.setToolVersion(1L);
        converter.setAppClass(1);
        converter.setCommandId(100);
        converter.setInputArgs("{{input_template}}");
        converter.setOutputArgs("{{output_template}}");
        converter.setProviderToolNum(999L);
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        int result = expoTemplateConverterMapper.insert(converter);
        assertEquals(1, result);
        assertNotNull(converter.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个Expo模板转换器
        ExpoTemplateConverter converter = new ExpoTemplateConverter();
        converter.setToolNum(54321L);
        converter.setToolVersion(2L);

        converter.setAppClass(2);
        converter.setCommandId(200);
        converter.setInputArgs("{{select_input}}");
        converter.setOutputArgs("{{select_output}}");
        converter.setProviderToolNum(888L);
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        expoTemplateConverterMapper.insert(converter);
        Long id = converter.getId();

        // 查询
        ExpoTemplateConverter selected = expoTemplateConverterMapper.selectById(id);
        assertNotNull(selected);
        assertEquals(54321L, selected.getToolNum());
        assertEquals(2L, selected.getToolVersion());
        assertEquals(2, selected.getAppClass());
        assertEquals(200, selected.getCommandId());
        assertEquals("{{select_input}}", selected.getInputArgs());
        assertEquals("{{select_output}}", selected.getOutputArgs());
        assertEquals(888L, selected.getProviderToolNum());
    }

    @Test
    void testUpdateById() {
        // 先插入一个Expo模板转换器
        ExpoTemplateConverter converter = new ExpoTemplateConverter();
        converter.setToolNum(98765L);
        converter.setToolVersion(1L);
        converter.setAppClass(1);
        converter.setCommandId(300);
        converter.setInputArgs("{{update_input}}");
        converter.setOutputArgs("{{update_output}}");
        converter.setProviderToolNum(777L);
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        expoTemplateConverterMapper.insert(converter);
        Long id = converter.getId();

        // 更新
        converter.setToolVersion(3L);
        converter.setAppClass(3);
        converter.setCommandId(350);
        converter.setInputArgs("{{updated_input}}");
        converter.setOutputArgs("{{updated_output}}");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("updater");

        int result = expoTemplateConverterMapper.updateById(converter);
        assertEquals(1, result);

        // 验证更新
        ExpoTemplateConverter updated = expoTemplateConverterMapper.selectById(id);
        assertEquals(3L, updated.getToolVersion());
        assertEquals(3, updated.getAppClass());
        assertEquals(350, updated.getCommandId());
        assertEquals("{{updated_input}}", updated.getInputArgs());
        assertEquals("{{updated_output}}", updated.getOutputArgs());
        assertEquals("updater", updated.getUpdateBy());
    }

    @Test
    void testDeleteById() {
        // 先插入一个Expo模板转换器
        ExpoTemplateConverter converter = new ExpoTemplateConverter();
        converter.setToolNum(11111L);
        converter.setToolVersion(1L);
        converter.setAppClass(1);
        converter.setCommandId(400);
        converter.setInputArgs("{{delete_input}}");
        converter.setOutputArgs("{{delete_output}}");
        converter.setProviderToolNum(666L);
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        expoTemplateConverterMapper.insert(converter);
        Long id = converter.getId();

        // 删除
        int result = expoTemplateConverterMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        ExpoTemplateConverter deleted = expoTemplateConverterMapper.selectById(id);
        assertNull(deleted);
    }
}