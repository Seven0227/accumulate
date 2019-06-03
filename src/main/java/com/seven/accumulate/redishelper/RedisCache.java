package com.seven.accumulate.redishelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @classDesc: 功能描述:
 * @Author: S_even_77
 * @createTime: Created in 10:02 2018/1/18
 * @version: v1.0
 * @copyright: 北京辰森
 * @email: wq@choicesoft.com.cn
 */

/**

    使用说明：
        1.ids:
            @RedisCache(tableName="POST", getIndex = 0, keyGetName = "getId")
        2.id
            @RedisCache(tableName = "ACCOUNT", getIndex = 0)
        3.getIndex默认为0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisCache {

    String tableName();

    /**
     * 对于多key组合的情况，主key是加注解的方法的第几个参数
     * @return
     */
    int getIndex() default 0;

    /**
     * value得到小key的方法名称
     * @return
     */
    String keyGetName() default "";


}
