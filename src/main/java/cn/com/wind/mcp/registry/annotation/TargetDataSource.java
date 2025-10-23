package cn.com.wind.mcp.registry.annotation;

import java.lang.annotation.*;

/**
 * 动态数据源注解
 * 用于标记方法或类需要使用的数据源
 *
 * @author system
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource {
    /**
     * 数据源名称
     * 默认为 ds_reader (读取数据源)
     * 可选值: ds_reader, ds_writer
     */
    String value() default "ds_reader";
}
