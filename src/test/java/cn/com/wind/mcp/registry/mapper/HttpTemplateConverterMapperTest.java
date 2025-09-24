package cn.com.wind.mcp.registry.mapper;

import java.time.LocalDateTime;

import cn.com.wind.mcp.registry.entity.HttpTemplateConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * HttpTemplateConverter Mapper 测试
 */
@SpringBootTest
@SpringJUnitConfig
@Transactional
public class HttpTemplateConverterMapperTest {

    @Autowired
    private HttpTemplateConverterMapper httpTemplateConverterMapper;

    @Test
    void testInsertHttpTemplateConverter() {
        HttpTemplateConverter converter = new HttpTemplateConverter();
        converter.setToolNum(12345L);
        converter.setToolVersion(1L);
        converter.setReqUrl("https://api.example.com/{{endpoint}}");
        converter.setReqMethod("POST");
        converter.setReqHeaders("{\"Content-Type\":\"application/json\",\"Authorization\":\"{{auth_token}}\"}");
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        int result = httpTemplateConverterMapper.insert(converter);
        assertEquals(1, result);
        assertNotNull(converter.getId());
    }

    @Test
    void testSelectById() {
        // 先插入一个HTTP模板转换器
        HttpTemplateConverter converter = new HttpTemplateConverter();
        converter.setToolNum(54321L);
        converter.setToolVersion(2L);
        converter.setReqUrl("https://api.test.com/{{path}}");
        converter.setReqMethod("GET");
        converter.setReqHeaders("{\"Accept\":\"application/json\"}");
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        httpTemplateConverterMapper.insert(converter);
        Long id = converter.getId();

        // 查询
        HttpTemplateConverter selected = httpTemplateConverterMapper.selectById(id);
        assertNotNull(selected);
        assertEquals(54321L, selected.getToolNum());
        assertEquals(2L, selected.getToolVersion());
        assertEquals("https://api.test.com/{{path}}", selected.getReqUrl());
        assertEquals("GET", selected.getReqMethod());
        assertEquals("{\"Accept\":\"application/json\"}", selected.getReqHeaders());
    }

    @Test
    void testUpdateById() {
        // 先插入一个HTTP模板转换器
        HttpTemplateConverter converter = new HttpTemplateConverter();
        converter.setToolNum(98765L);
        converter.setToolVersion(1L);
        converter.setReqUrl("https://api.update.com/{{endpoint}}");
        converter.setReqMethod("PUT");
        converter.setReqHeaders("{\"Content-Type\":\"application/json\"}");
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        httpTemplateConverterMapper.insert(converter);
        Long id = converter.getId();

        // 更新
        converter.setToolVersion(2L);
        converter.setReqUrl("https://api.updated.com/{{new_endpoint}}");
        converter.setReqMethod("PATCH");
        converter.setReqHeaders("{\"Content-Type\":\"application/json\",\"X-Version\":\"v2\"}");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("updater");

        int result = httpTemplateConverterMapper.updateById(converter);
        assertEquals(1, result);

        // 验证更新
        HttpTemplateConverter updated = httpTemplateConverterMapper.selectById(id);
        assertEquals(2L, updated.getToolVersion());
        assertEquals("https://api.updated.com/{{new_endpoint}}", updated.getReqUrl());
        assertEquals("PATCH", updated.getReqMethod());
        assertEquals("{\"Content-Type\":\"application/json\",\"X-Version\":\"v2\"}", updated.getReqHeaders());
        assertEquals("updater", updated.getUpdateBy());
    }

    @Test
    void testDeleteById() {
        // 先插入一个HTTP模板转换器
        HttpTemplateConverter converter = new HttpTemplateConverter();
        converter.setToolNum(11111L);
        converter.setToolVersion(1L);
        converter.setReqUrl("https://api.delete.com/{{endpoint}}");
        converter.setReqMethod("DELETE");
        converter.setReqHeaders("{\"Content-Type\":\"application/json\"}");
        converter.setCreateTime(LocalDateTime.now());
        converter.setCreateBy("system");
        converter.setUpdateTime(LocalDateTime.now());
        converter.setUpdateBy("system");

        httpTemplateConverterMapper.insert(converter);
        Long id = converter.getId();

        // 删除
        int result = httpTemplateConverterMapper.deleteById(id);
        assertEquals(1, result);

        // 验证删除
        HttpTemplateConverter deleted = httpTemplateConverterMapper.selectById(id);
        assertNull(deleted);
    }
}