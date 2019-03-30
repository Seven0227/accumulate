package com.seven.accumulate.jdk;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 19:13 2018/10/11
 */
public class P3 {
    public static void main(String[] args) {

        List<Student> students1 = Arrays.asList(
                new Student("A", 90),
                new Student("B", 80),
                new Student("B", 70)
        );

        Map<String, Integer> collect = students1.stream().collect(Collectors.toMap(Student::getName,
                Student::getScore, (s, v) -> s));
        System.out.println(collect);


    }
}
