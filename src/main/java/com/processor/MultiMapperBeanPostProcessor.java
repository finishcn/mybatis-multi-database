package com.processor;

import com.framework.autoconfigure.jdbc.MultiConfiguration;
import com.framework.autoconfigure.jdbc.MultiDataSourceProperties;
import com.framework.multi.BaseMapperInvocationHandler;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
@ConditionalOnBean(MultiDataSourceProperties.class)
public class MultiMapperBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class mapperClass = MultiConfiguration.getMapperClass(beanName);
        if (null != mapperClass) {
            MapperFactoryBean mapper = (MapperFactoryBean) bean;
            Object obj = null;
            try {
                obj = mapper.getObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Class baseMapperClass = MultiConfiguration.getBaseMapperClass(beanName);
            MultiConfiguration.remove(beanName);
            return new BaseMapperInvocationHandler((Proxy) obj, baseMapperClass);
        }
        return bean;
    }

}
