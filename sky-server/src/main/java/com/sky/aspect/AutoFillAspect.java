package com.sky.aspect;

import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 *
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //mapper包下的所有类与方法都要进行自动填充 并且 有autofill注解
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("自动填充字段...");
        // 获取数据库操作类型
        String methodName = joinPoint.getSignature().getName();
        // 获取被拦截方法的参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0) {
            return;
        }
        // 获取被拦截方法的第一个参数
        Object arg = args[0];
        // 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        // 根据操作类型赋值(使用反射)
        if (methodName.startsWith("insert")) {
            // 插入操作，填充创建时间、修改时间、创建人、修改人
            try {
                arg.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class).invoke(arg, now);
                arg.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(arg, now);
                arg.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(arg, currentId);
                arg.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(arg, currentId);
            } catch (Exception e) {
                log.error("自动填充失败", e);
            }
        } else {
            // 更新操作，填充修改时间、修改人
            try {
                arg.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(arg, now);
                arg.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(arg, currentId);
            } catch (Exception e) {
                log.error("自动填充失败", e);
            }
        }
    }
}
