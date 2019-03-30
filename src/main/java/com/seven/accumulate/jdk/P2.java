package com.seven.accumulate.jdk;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 18:30 2018/10/11
 */
public class P2 {
    public static void main(String[] args) {
        //创建流
        //1.数组
        //1.1 Arrays.stream
        //1.1.1 Arrays.stream之基本类型
        int[] arr = new int[]{1, 2, 34, 5};
        IntStream intStream = Arrays.stream(arr);
//        intStream.forEach(System.out::println);
        //1.1.2 Arrays.stream引用类型
        Student[] students = new Student[]{new Student("A", 90), new Student("B", 80)};
        Stream<Student> stream = Arrays.stream(students);

        //1.2 Stream.of()
        Stream<Integer> integerStream = Stream.of(1, 2, 24, 5, 65);
        Stream<int[]> arr1 = Stream.of(arr, arr);
        arr1.forEach(System.out::println);

        //2. 通过集合
        List<Student> students1 = Arrays.asList(
                new Student("A", 90),
                new Student("B", 80)
        );
        students1.stream();

        //3.创建空流
        Stream<Integer> empty = Stream.empty();

        //4.创建无限流
        Stream.generate(() -> "number" + new Random().nextInt()).limit(10).forEach(System.out::println);
        Stream.generate(Student::new).limit(20).forEach(System.out::println);
        Stream.generate(() -> new Student("name", 2)).limit(2).forEach(System.out::println);

        //5.创建规律的无限流
        Stream.iterate(0, x -> x + 1).limit(10).forEach(System.out::println);
        Stream.iterate(0, x -> x).limit(10).forEach(System.out::println);
//        Stream.iterate(0, UnaryOperator.identity()).limit()


    }
}
