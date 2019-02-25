package com.light.privateMovies.reptile.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用来注解
 *
 * @see com.light.privateMovies.reptile.StepMethod 的处理函数
 * 1.针对三个处理函数的注解,如访问的url,要获取的目的信息
 * 2.针对类注解,类注解的信息是为了提供给
 * @see com.light.privateMovies.reptile.Reptile 使用
 * 那么reptile就应该创建一部分新的代码用来解析该部分
 */
//TODO: 可以尝试注解用来配置,StepMethod每一步处理时需要的一些参数
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.TYPE})
public @interface Step {
    String url() default "";

    String target() default "";
}
