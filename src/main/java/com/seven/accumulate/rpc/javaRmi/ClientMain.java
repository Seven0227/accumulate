package com.seven.accumulate.rpc.javaRmi;

import java.rmi.Naming;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 10:55 2019/2/26
 */
public class ClientMain {
    public static void main(String[] args) throws Exception {
        //服务引入
        HelloService helloService = (HelloService) Naming.lookup("rmi://localhost:8801/helloService");

        //调用远程方法
        System.out.println("服务提供方提供返回的结果是：" + helloService.sayHello("77"));
    }
}
