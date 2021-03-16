package com.framework.multi;

import org.mybatis.spring.mapper.MapperFactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class BaseMapperInvocationHandler<T> extends MapperFactoryBean<T> implements InvocationHandler {

    private Proxy mapper;

    private Class baseMapperClass;

    private Map<String, Method> methodMap = new HashMap<>();

    public BaseMapperInvocationHandler(Proxy mapper, Class baseMapperClass) {
        this.baseMapperClass = baseMapperClass;
        this.mapper = mapper;
        for (Method method : mapper.getClass().getMethods()) {
            if (method.getDeclaringClass() != Object.class) {
                methodMap.put(method.getName(), method);
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(args);
        } else {
            Method m = methodMap.get(method.getName());
            return m.invoke(mapper, args);
        }
    }

    @Override
    public T getObject() {
        return (T) Proxy.newProxyInstance(baseMapperClass.getClassLoader(), new Class<?>[]{baseMapperClass}, this);
    }
}
