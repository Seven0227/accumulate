package com.seven.accumulate.rpc.javaRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 10:33 2019/2/26
 */
public interface HelloService extends Remote {
    String sayHello(String sameone) throws RemoteException;
}
