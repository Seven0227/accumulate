package com.seven.accumulate.redishelper;

import springfox.documentation.annotations.Cacheable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @classDesc: 功能描述:
 * @Author: S_even_77
 * @createTime: Created in 17:29 2018/1/25
 * @version: v1.0
 * @copyright: 北京辰森
 * @email: wq@choicesoft.com.cn
 */


/**
 @RedisMapCache(type= ApplicationDto.class, tableName = "APPLICATION", getIndex = 0)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisMapCache {


    /**
     * 存到缓存中的value的类型
     * @return
     */
    Class type();

    String tableName();
    /**
     * 存到缓存中小ke, 是加注解的方法的第几个参数
     * @return
     */
    int getIndex() default 0;
}
