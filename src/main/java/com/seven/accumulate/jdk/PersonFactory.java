package com.seven.accumulate.jdk;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 13:51 2018/10/11
 */
public interface PersonFactory <P extends Person> {
    P create(String name, Integer age);
}
