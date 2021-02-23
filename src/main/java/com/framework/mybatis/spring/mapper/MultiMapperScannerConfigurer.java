package com.framework.mybatis.spring.mapper;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.Assert.notNull;

/**
 * mybatis MapperScannerConfigurer扩展
 *
 * @author liyu
 * @see MapperScannerConfigurer
 */
public class MultiMapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

    private String basePackage;

    private boolean addToConfig = true;

    private String lazyInitialization;

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;

    private String sqlSessionFactoryBeanName;

    private String sqlSessionTemplateBeanName;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private Class<? extends MapperFactoryBean> mapperFactoryBeanClass;

    private ApplicationContext applicationContext;

    private String beanName;

    private boolean processPropertyPlaceHolders;

    private BeanNameGenerator nameGenerator;

    private String apps;

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public void setLazyInitialization(String lazyInitialization) {
        this.lazyInitialization = lazyInitialization;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> superClass) {
        this.markerInterface = superClass;
    }

    @Deprecated
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public void setSqlSessionTemplateBeanName(String sqlSessionTemplateName) {
        this.sqlSessionTemplateBeanName = sqlSessionTemplateName;
    }

    @Deprecated
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryName;
    }

    public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
        this.processPropertyPlaceHolders = processPropertyPlaceHolders;
    }

    public void setMapperFactoryBeanClass(Class<? extends MapperFactoryBean> mapperFactoryBeanClass) {
        this.mapperFactoryBeanClass = mapperFactoryBeanClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public BeanNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public void setNameGenerator(BeanNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    public String getApps() {
        return apps;
    }

    public void setApps(String apps) {
        this.apps = apps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.basePackage, "Property 'basePackage' is required");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // left intentionally blank
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }

        MultiClassPathMapperScanner scanner = new MultiClassPathMapperScanner(registry);
        scanner.setAddToConfig(this.addToConfig);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setSqlSessionFactory(this.sqlSessionFactory);
        scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
        scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
        scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        scanner.setMapperFactoryBeanClass(this.mapperFactoryBeanClass);
        if (StringUtils.hasText(this.apps)) {
            Map<String, Object> map = new YamlJsonParser().parseMap(apps);
            scanner.setApps(map.keySet());
        }
        if (StringUtils.hasText(lazyInitialization)) {
            scanner.setLazyInitialization(Boolean.parseBoolean(lazyInitialization));
        }
        scanner.registerFilters();
        scanner.scan(
                StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    private void processPropertyPlaceHolders() {
        Map<String, PropertyResourceConfigurer> prcs = applicationContext.getBeansOfType(PropertyResourceConfigurer.class,
                false, false);

        if (!prcs.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                    .getBeanDefinition(beanName);

            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, mapperScannerBean);

            for (PropertyResourceConfigurer prc : prcs.values()) {
                prc.postProcessBeanFactory(factory);
            }

            PropertyValues values = mapperScannerBean.getPropertyValues();

            this.basePackage = updatePropertyValue("basePackage", values);
            this.sqlSessionFactoryBeanName = updatePropertyValue("sqlSessionFactoryBeanName", values);
            this.sqlSessionTemplateBeanName = updatePropertyValue("sqlSessionTemplateBeanName", values);
            this.lazyInitialization = updatePropertyValue("lazyInitialization", values);
        }
        this.basePackage = Optional.ofNullable(this.basePackage).map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.sqlSessionFactoryBeanName = Optional.ofNullable(this.sqlSessionFactoryBeanName)
                .map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.sqlSessionTemplateBeanName = Optional.ofNullable(this.sqlSessionTemplateBeanName)
                .map(getEnvironment()::resolvePlaceholders).orElse(null);
        this.lazyInitialization = Optional.ofNullable(this.lazyInitialization).map(getEnvironment()::resolvePlaceholders)
                .orElse(null);
    }

    private Environment getEnvironment() {
        return this.applicationContext.getEnvironment();
    }

    private String updatePropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);

        if (property == null) {
            return null;
        }

        Object value = property.getValue();

        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        } else {
            return null;
        }
    }
}
