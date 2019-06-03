package com.seven.accumulate.redishelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @classDesc: 功能描述:
 * @Author: S_even_77
 * @createTime: Created in 10:04 2018/1/18
 * @version: v1.0
 * @copyright: 北京辰森
 * @email: wq@choicesoft.com.cn
 */

/**
     使用说明：
     @RedisEvict(getIndex = 0,tableName = "POST")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisEvict {
    /**
     * 存到缓存中小ke, 是加注解的方法的第几个参数
     * @return
     */
    int getIndex() default 0;

    String tableName();
}
