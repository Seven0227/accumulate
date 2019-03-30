package com.seven.accumulate.rpc.rpcSelf.service;

/**
 * @classDesc: ()
 * @Author:
 * @createTime: Created in 17:52 2019/2/26
 */
public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String content) {
        return "Hello," + content;
    }
}
