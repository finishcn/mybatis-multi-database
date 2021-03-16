package example.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.framework.autoconfigure.jdbc.MultiDataSourceProperties;
import com.framework.mybatis.spring.MultiSqlSessionFactoryBean;
import com.framework.mybatis.spring.annotation.MultiMapperScan;

/**
 * 鏁版嵁婧愰厤缃ず渚�
 */
@Configuration
@MultiMapperScan(basePackages = "com.p1.dao", sqlSessionTemplateRef = "p1SessionTemplate", properties = "spring.multidatasource.p1ds")
public class P1DSConfiguration {

	@Primary
	@Bean(name = "p1DataSourceProperties")
	@ConfigurationProperties(prefix = "spring.multidatasource.p1ds")
	public MultiDataSourceProperties dataDataSourceProperties() {
		return new MultiDataSourceProperties();
	}

	@Primary
	@Bean("p1DataSource")
	public DataSource dataDataSource(
			@Qualifier("p1DataSourceProperties") MultiDataSourceProperties dataSourceProperties) {
		return dataSourceProperties.initializeDataSourceBuilder().build();
	}

	@Bean(name = "p1SqlSessionFactory")
	public SqlSessionFactory dataSqlSessionFactory(@Qualifier("p1DataSource") DataSource dataSource,
			@Qualifier("p1DataSourceProperties") MultiDataSourceProperties dataSourceProperties) throws Exception {
		MultiSqlSessionFactoryBean sqlSessionFactory = new MultiSqlSessionFactoryBean(dataSourceProperties);
		sqlSessionFactory.setDataSource(dataSource);
		return sqlSessionFactory.getObject();
	}

	@Bean(name = "p1SessionTemplate")
	public SqlSessionTemplate dataSessionTemplate(
			@Qualifier("p1SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
