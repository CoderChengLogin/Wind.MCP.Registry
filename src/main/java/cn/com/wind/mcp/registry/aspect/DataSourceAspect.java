package cn.com.wind.mcp.registry.aspect;

import cn.com.wind.mcp.registry.annotation.TargetDataSource;
import cn.com.wind.mcp.registry.config.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据源切换AOP切面
 * 拦截标注了@TargetDataSource注解的方法，实现动态数据源切换
 *
 * @author system
 */
@Slf4j
@Aspect
@Order(1) // 确保在事务切面之前执行
@Component
public class DataSourceAspect {

    /**
     * 定义切点：拦截所有标注了@TargetDataSource注解的方法
     */
    @Pointcut("@annotation(cn.com.wind.mcp.registry.annotation.TargetDataSource) " +
            "|| @within(cn.com.wind.mcp.registry.annotation.TargetDataSource)")
    public void dataSourcePointcut() {
    }

    /**
     * 环绕通知：在方法执行前后进行数据源切换
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("dataSourcePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取@TargetDataSource注解
        TargetDataSource targetDataSource = method.getAnnotation(TargetDataSource.class);

        // 如果方法上没有注解，尝试从类上获取
        if (targetDataSource == null) {
            targetDataSource = joinPoint.getTarget().getClass().getAnnotation(TargetDataSource.class);
        }

        // 记录原始数据源，用于恢复
        String originalDataSource = DynamicDataSourceContextHolder.getDataSource();

        try {
            if (targetDataSource != null) {
                String dataSourceKey = targetDataSource.value();
                log.info("切换数据源: {} -> {}, 方法: {}.{}",
                        originalDataSource, dataSourceKey,
                        joinPoint.getTarget().getClass().getSimpleName(),
                        method.getName());

                // 设置数据源
                DynamicDataSourceContextHolder.setDataSource(dataSourceKey);
            }

            // 执行目标方法
            return joinPoint.proceed();

        } finally {
            // 方法执行完毕后，恢复原始数据源
            if (targetDataSource != null) {
                log.debug("恢复数据源: {}", originalDataSource);
                if (originalDataSource != null) {
                    DynamicDataSourceContextHolder.setDataSource(originalDataSource);
                } else {
                    DynamicDataSourceContextHolder.clearDataSource();
                }
            }
        }
    }
}
