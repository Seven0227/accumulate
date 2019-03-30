package com.seven.accumulate.rpc.rpcSelf.framework;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 17:49 2019/2/26
 */
public class ConsumerProxy {

    /**
     * 服务消费代理接口
     */
    public static <T> T consume(final Class<T> interfaceClass, final String host, final int port) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = new Socket(host, port);

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                output.writeUTF(method.getName());
                output.writeObject(args);
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Object result = input.readObject();
                if (result instanceof Throwable) {
                    throw (Throwable) result;
                }
                return result;
            }
        });
    }
}
