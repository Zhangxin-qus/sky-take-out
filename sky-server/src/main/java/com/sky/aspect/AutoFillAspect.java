package com.sky.aspect;



import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/*
    自定义切面，实现公共字段自动填充逻辑
*/
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /*
        切入点
    */
    // 前面的条件是缩小的扫描范围的，后面的注解扫描进行再筛选
    //  拦截的返回值是为所有的  mapper包下的所有的类所有的方法，其中参数为任意   并且只拦截其中带有AutoFill注解的
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    /*
        使用前置通知，在通知中进行公共字段赋值
    */
    // 参数放切入点表达式，但是前面已经赋值过方法，现在直接填入方法名就行
    @Before("autoFillPointCut()")
    //                       连接点
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature =(MethodSignature) joinPoint.getSignature(); // 使用连接点对象获得签名对象
            //由于获取的是方法对象，所以将获取的Signature向下转型为MethodSignature
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); // 获得方法上的注解对象
        OperationType operationType = autoFill.value(); // 获得数据库操作类型

        //获取到当前被拦截的方法的参数--实体对象 例如：void insert(Employee employee);中的employee 当然获取的对象可以为任意类型
        Object[] args = joinPoint.getArgs();
            // 排除异常情况
        if (args == null || args.length == 0) {
            return;
        }

            // 约定实体对象放在第一个参数位置
        Object entity = args[0];


        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的操作类型，为对应的属性通过反射来赋信
        if (operationType == OperationType.INSERT) {
            try {
                // 为四个公共字段赋值
                // 获取各个方法，输入方法名和方法类型获取
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                // 使用常量类可以防止方法名写错
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射为各个属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if (operationType == OperationType.UPDATE) {
            try {
                // 为两个公共字段赋值
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射为各个属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
