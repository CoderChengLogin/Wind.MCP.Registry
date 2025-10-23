package cn.com.wind.mcp.registry.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 动态数据源上下文持有者
 * 使用ThreadLocal保存当前线程使用的数据源标识
 *
 * @author system
 */
@Slf4j
public class DynamicDataSourceContextHolder {

    /**
     * 数据源常量
     */
    public static final String DS_READER = "ds_reader";
    public static final String DS_WRITER = "ds_writer";
    /**
     * 使用ThreadLocal保存数据源key，确保线程安全
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 获取当前数据源
     *
     * @return 数据源key
     */
    public static String getDataSource() {
        String dataSource = CONTEXT_HOLDER.get();
        if (dataSource == null) {
            // 默认使用读数据源
            return DS_READER;
        }
        return dataSource;
    }

    /**
     * 设置数据源
     *
     * @param dataSourceKey 数据源key
     */
    public static void setDataSource(String dataSourceKey) {
        log.debug("切换数据源至: {}", dataSourceKey);
        CONTEXT_HOLDER.set(dataSourceKey);
    }

    /**
     * 清除数据源
     */
    public static void clearDataSource() {
        log.debug("清除数据源: {}", CONTEXT_HOLDER.get());
        CONTEXT_HOLDER.remove();
    }
}
