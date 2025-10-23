package cn.com.wind.mcp.registry.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源路由
 * 继承AbstractRoutingDataSource实现动态数据源切换
 *
 * @author system
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 决定使用哪个数据源
     * 返回的key必须与配置的数据源key匹配
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = DynamicDataSourceContextHolder.getDataSource();
        log.debug("当前使用的数据源: {}", dataSource);
        return dataSource;
    }
}
