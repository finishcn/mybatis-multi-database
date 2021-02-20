package com.framework.mybatis.spring.annotation;

import org.mybatis.spring.annotation.MapperScannerRegistrar;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * mybatis MapperScan扩展
 *
 * @author liyu
 * @see MapperScans
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MultiMapperScannerRegistrar.RepeatingRegistrar.class)
public @interface MultiMapperScans {
    MultiMapperScan[] value();
}
