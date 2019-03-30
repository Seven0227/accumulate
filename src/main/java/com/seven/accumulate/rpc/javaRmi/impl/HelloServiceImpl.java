package com.seven.accumulate.rpc.javaRmi.impl;

import com.seven.accumulate.rpc.javaRmi.HelloService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 17:38 2019/2/26
 */
public class HelloServiceImpl extends UnicastRemoteObject implements HelloService {
    public HelloServiceImpl() throws RemoteException {
    }

    @Override
    public String sayHello(String someone) throws RemoteException {
        return "Hello, " + someone;
    }
}
