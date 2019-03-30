package com.seven.accumulate.jdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 13:01 2018/10/11
 */
public class P1 {
    public static void main(String[] args) {
//        Converter<String, Integer> converter = new Converter<String, Integer>() {
//            @Override
//            public Integer convert(String from) {
//                return Integer.valueOf(from);
//            }
//        };

//        Converter<String, Integer> converter1 = from -> Integer.valueOf(from);
//        System.out.println(converter1.convert("123"));
//
//        Converter<String, Integer> converter2 = Integer::valueOf;
//        System.out.println(converter2.convert("1234"));
//
//        PersonFactory<Person> personFactory = Person::new;
//        Person person = personFactory.create("七大爷", 18);
//        System.out.println(person);
//
//        Function<Integer, Integer> times2 = i -> i * 2;
//        Function<Integer, Integer> squared = i -> i * i;
//
//        System.out.println(times2.apply(4));
//        System.out.println(squared.apply(4));
//
//        System.out.println(times2.compose(squared).apply(4));


        List<String> stringCollection = new ArrayList<>();
        stringCollection.add("ddd2");
        stringCollection.add("aaa2");
        stringCollection.add("bbb1");
        stringCollection.add("aaa1");
        stringCollection.add("bbb3");
        stringCollection.add("ccc");
        stringCollection.add("bbb2");
        stringCollection.add("ddd1");

        Consumer<String> consumer = s -> System.out.println(s);

        Consumer<String> consumer1 = System.out::println;

        stringCollection.stream().filter(s -> s.startsWith("a")).forEach(System.out::println);
    }
}
