package com.seven.accumulate.rpc.javaRmi;


import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMISocketFactory;
import com.seven.accumulate.rpc.javaRmi.impl.HelloServiceImpl;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 10:51 2019/2/26
 */
public class ServerMain {
    public static void main(String[] args) throws Exception {
//        //创建服务
//        HelloService helloService = new HelloServiceImpl();
//        //注册服务
//        LocateRegistry.createRegistry(8801);
//        Naming.bind("rmi://localhost:8801/helloService", helloService);
//        System.out.println("服务提供方开始提供服务");


        LocateRegistry.createRegistry(8801);
        //指定通信端口，防止被防火墙拦截
        RMISocketFactory.setSocketFactory(new CustomerSocketFactory());
        HelloService helloService = new HelloServiceImpl();
        Naming.bind("rmi://localhost:8801/helloService", helloService);
        System.out.println("服务提供方开始提供服务");
    }
}
