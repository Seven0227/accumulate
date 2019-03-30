package com.seven.accumulate.rpc.javaRmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 11:11 2019/2/26
 */
public class CustomerSocketFactory extends RMISocketFactory {
    //指定通信端口，防止被防火墙拦截
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return new Socket(host, port);
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        if (port == 0) {
            port = 8501;
        }
        System.out.println("rmi notify port:" + port);
        return new ServerSocket(port);
    }
}
