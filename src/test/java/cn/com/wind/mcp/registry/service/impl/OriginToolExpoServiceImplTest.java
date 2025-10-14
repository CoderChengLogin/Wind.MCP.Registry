package cn.com.wind.mcp.registry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class OriginToolExpoServiceImplTest {

    private OriginToolExpoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = spy(new OriginToolExpoServiceImpl());
    }

    @Test
    void testCountByCreateBy() {
        doReturn(6L).when(service).count(any(QueryWrapper.class));
        long count = service.countByCreateBy("testuser");
        assertEquals(6L, count);
    }

    @Test
    void testCountByProviderId() {
        doReturn(12L).when(service).count(any(QueryWrapper.class));
        long count = service.countByProviderId(100L);
        assertEquals(12L, count);
    }
}
