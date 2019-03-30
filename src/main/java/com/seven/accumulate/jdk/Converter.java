package com.seven.accumulate.jdk;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 13:28 2018/10/11
 */
@FunctionalInterface
public interface Converter<F, T> {
    T convert(F from);
    default int a() {
        return 1;
    }
    static int b() {
        return 2;
    }
}
