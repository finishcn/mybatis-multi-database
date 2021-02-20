package com.framework.mybatis.spring.mapper;

import com.framework.multi.bulider.DynamicClassBuilder;
import com.framework.multi.loader.DynamicClassLoader;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * mybatis ClassPathMapperScanner扩展
 *
 * @author liyu
 * @see ClassPathMapperScanner
 */
public class MultiClassPathMapperScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiClassPathMapperScanner.class);

    private boolean addToConfig = true;

    private boolean lazyInitialization;

    private SqlSessionFactory sqlSessionFactory;

    private SqlSessionTemplate sqlSessionTemplate;

    private String sqlSessionTemplateBeanName;

    private String sqlSessionFactoryBeanName;

    private Class<? extends Annotation> annotationClass;

    private Class<?> markerInterface;

    private BeanDefinitionRegistry registry;

    private String[] apps;

    private Class<? extends MapperFactoryBean> mapperFactoryBeanClass = MapperFactoryBean.class;

    public MultiClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        this.registry = registry;
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setLazyInitialization(boolean lazyInitialization) {
        this.lazyInitialization = lazyInitialization;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public void setSqlSessionTemplateBeanName(String sqlSessionTemplateBeanName) {
        this.sqlSessionTemplateBeanName = sqlSessionTemplateBeanName;
    }

    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }

    public String[] getApps() {
        return apps;
    }

    public void setApps(String[] apps) {
        this.apps = apps;
    }

    @Deprecated
    public void setMapperFactoryBean(MapperFactoryBean<?> mapperFactoryBean) {
        this.mapperFactoryBeanClass = mapperFactoryBean == null ? MapperFactoryBean.class : mapperFactoryBean.getClass();
    }

    public void setMapperFactoryBeanClass(Class<? extends MapperFactoryBean> mapperFactoryBeanClass) {
        this.mapperFactoryBeanClass = mapperFactoryBeanClass == null ? MapperFactoryBean.class : mapperFactoryBeanClass;
    }

    public void registerFilters() {
        boolean acceptAllInterfaces = true;

        // if specified, use the given annotation and / or marker interface
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        // override AssignableTypeFilter to ignore matches on the actual marker interface
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        if (acceptAllInterfaces) {
            // default include filter that accepts all classes
            addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
        }

        // exclude package-info.java
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return className.endsWith("package-info");
        });
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        try {
            DynamicClassLoader classLoader = new DynamicClassLoader();
            for (BeanDefinitionHolder baseholder : beanDefinitions) {
                if (baseholder.getBeanName().startsWith("multi")) {
                    processMultiBeanDefinitions(classLoader, baseholder);
                } else {
                    processBeanDefinitions(baseholder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return beanDefinitions;
    }

    /**
     * 原实例配置
     */
    private void processBeanDefinitions(BeanDefinitionHolder holder) {
        GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
        String beanClassName = definition.getBeanClassName();
        LOGGER.debug(() -> "Creating MapperFactoryBean with name '" + holder.getBeanName() + "' and '" + beanClassName
                + "' mapperInterface");

        // the mapper interface is the original class of the bean
        // but, the actual class of the bean is MapperFactoryBean
        definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName); // issue #59
        definition.setBeanClass(this.mapperFactoryBeanClass);
        processBeanDefinitions(definition);
    }

    /**
     * 多实例配置
     */
    private void processMultiBeanDefinitions(DynamicClassLoader classLoader, BeanDefinitionHolder baseholder) throws Exception {
        String baseBeanName = baseholder.getBeanName();
        //移除基础bean定义
        this.registry.removeBeanDefinition(baseBeanName);
        baseBeanName = baseBeanName.substring(5);
        GenericBeanDefinition baseDefinition = (GenericBeanDefinition) baseholder.getBeanDefinition();
        String baseBeanClassName = baseDefinition.getBeanClassName();
        for (String app : apps) {
            //创建新bean
            String className = app.substring(0, 1).toUpperCase() + app.substring(1) + baseBeanName;
            String beanName = app + baseBeanName;
            Class clz = DynamicClassBuilder.createMapperClass(classLoader, className, baseBeanClassName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clz);
            AbstractBeanDefinition definition = builder.getBeanDefinition();
            // 原mybatis代码
            definition.getConstructorArgumentValues().addGenericArgumentValue(clz);
            definition.setBeanClass(this.mapperFactoryBeanClass);
            processBeanDefinitions(definition);
            registry.registerBeanDefinition(beanName, definition);
        }
    }

    private void processBeanDefinitions(AbstractBeanDefinition definition) {
        definition.getPropertyValues().add("addToConfig", this.addToConfig);
        boolean explicitFactoryUsed = false;
        if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
            definition.getPropertyValues().add("sqlSessionFactory",
                    new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
            explicitFactoryUsed = true;
        } else if (this.sqlSessionFactory != null) {
            definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
            explicitFactoryUsed = true;
        }

        if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
            if (explicitFactoryUsed) {
                LOGGER.warn(
                        () -> "Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
            }
            definition.getPropertyValues().add("sqlSessionTemplate",
                    new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
            explicitFactoryUsed = true;
        } else if (this.sqlSessionTemplate != null) {
            if (explicitFactoryUsed) {
                LOGGER.warn(
                        () -> "Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
            }
            definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
            explicitFactoryUsed = true;
        }

        if (!explicitFactoryUsed) {
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
        definition.setLazyInit(lazyInitialization);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            LOGGER.warn(() -> "Skipping MapperFactoryBean with name '" + beanName + "' and '"
                    + beanDefinition.getBeanClassName() + "' mapperInterface" + ". Bean already defined with the same name!");
            return false;
        }
    }

}
