package example.config;

import com.framework.autoconfigure.jdbc.MultiDataSourceProperties;
import com.framework.mybatis.spring.MultiSqlSessionFactoryBean;
import com.framework.mybatis.spring.annotation.MultiMapperScan;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置示例
 */
@Configuration
@MultiMapperScan(basePackages = "com.p2.dao", sqlSessionTemplateRef = "p2SessionTemplate", properties = "spring.multidatasource.p2ds")
public class P2DSConfiguration {

    @Primary
    @Bean(name = "p2DataSourceProperties")
    @ConfigurationProperties(prefix = "spring.multidatasource.p2ds")
    public MultiDataSourceProperties dataDataSourceProperties() {
        return new MultiDataSourceProperties();
    }

    @Primary
    @Bean("p2DataSource")
    public DataSource dataDataSource(@Qualifier("p2DataSourceProperties") MultiDataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "p2SqlSessionFactory")
    public SqlSessionFactory dataSqlSessionFactory(@Qualifier("p2DataSource") DataSource dataSource
            , @Qualifier("p2DataSourceProperties") MultiDataSourceProperties dataSourceProperties) throws Exception {
        MultiSqlSessionFactoryBean sqlSessionFactory = new MultiSqlSessionFactoryBean(dataSourceProperties);
        sqlSessionFactory.setDataSource(dataSource);
        return sqlSessionFactory.getObject();
    }

    @Bean(name = "p2SessionTemplate")
    public SqlSessionTemplate dataSessionTemplate(@Qualifier("p2SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
