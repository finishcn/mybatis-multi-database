package com.processor;

import com.framework.autoconfigure.jdbc.MultiConfiguration;
import com.framework.multi.BaseMapperInvocationHandler;
import lombok.SneakyThrows;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
public class MultiMapperBeanPostProcessor implements BeanPostProcessor {

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class mapperClass = MultiConfiguration.getMapperClass(beanName);
        if (null != mapperClass) {
            MapperFactoryBean mapper = (MapperFactoryBean) bean;
            Object obj = mapper.getObject();
            Class baseMapperClass = MultiConfiguration.getBaseMapperClass(beanName);
            MultiConfiguration.remove(beanName);
            return new BaseMapperInvocationHandler((Proxy) obj, baseMapperClass);
        }
        return bean;
    }

}
