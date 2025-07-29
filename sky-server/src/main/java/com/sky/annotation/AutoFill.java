package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 将此注解加到方法上
@Target(ElementType.METHOD)
// 保留时间(生命周期)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 使用枚举类，指定返回的数据类型为insert和update
    OperationType value(); // value为注解中的方法名

}
