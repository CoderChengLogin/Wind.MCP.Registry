package cn.com.wind.mcp.registry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OriginToolHttpServiceImpl单元测试类
 * <p>
 * 测试原始HTTP工具服务的业务逻辑
 * </p>
 *
 * @author system
 * @date Created in 2025-10-14
 */
class OriginToolHttpServiceImplTest {

    private OriginToolHttpServiceImpl service;

    @BeforeEach
    void setUp() {
        service = spy(new OriginToolHttpServiceImpl());
    }

    /**
     * 测试按创建人统计工具数量
     * 应该返回正确的统计结果
     */
    @Test
    void testCountByCreateBy() {
        doReturn(8L).when(service).count(any(QueryWrapper.class));

        long count = service.countByCreateBy("testuser");
        assertEquals(8L, count);

        verify(service, times(1)).count(any(QueryWrapper.class));
    }

    /**
     * 测试按创建人统计工具数量 - 空字符串
     * 应该能够处理空字符串
     */
    @Test
    void testCountByCreateBy_EmptyString() {
        doReturn(0L).when(service).count(any(QueryWrapper.class));

        long count = service.countByCreateBy("");
        assertEquals(0L, count);
    }

    /**
     * 测试按提供者ID统计工具数量
     * 应该返回正确的统计结果
     */
    @Test
    void testCountByProviderId() {
        doReturn(15L).when(service).count(any(QueryWrapper.class));

        long count = service.countByProviderId(100L);
        assertEquals(15L, count);

        verify(service, times(1)).count(any(QueryWrapper.class));
    }

    /**
     * 测试按提供者ID统计工具数量 - 0个结果
     * 应该返回0
     */
    @Test
    void testCountByProviderId_Zero() {
        doReturn(0L).when(service).count(any(QueryWrapper.class));

        long count = service.countByProviderId(999L);
        assertEquals(0L, count);
    }

    /**
     * 测试按提供者ID统计工具数量 - null ID
     * 应该能够处理null参数
     */
    @Test
    void testCountByProviderId_Null() {
        doReturn(0L).when(service).count(any(QueryWrapper.class));

        long count = service.countByProviderId(null);
        assertEquals(0L, count);
    }
}
