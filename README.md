# mybatis-multi-database
springboot mybatis多数据库（实例）配置扩展插件

如果你的系统有多个项目的数据库实例都在一个数据库系统中（数据源），并且所需要的功能类型。如果你也在用MyBatis，建议尝试该插件，这一定能节省大量的工作。

多数据库实例扩展插件支持通过一个基础通用Mapper，通过配置创建不同项目的Mapper，操作在同一个数据库系统中不同项目的数据库实例，避免创建多个项目相同的Mapper文件。

目前版本只支持springboot框架，通过使用Maven方式引入

springboot自动配置方式

spring:
  multidatasource:
    
    driver-class-name: com.mysql.jdbc.Driver
    
    username: root
    
    password: mima123456
    
    url: jdbc:mysql://127.0.0.1:3306/view?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&useSSL=false
    
    mapper-locations: classpath*:mappers/data/*.xml,classpath*:mappers/multi/*.xml
    
    apps: '{project1: database1,project2: database2,project3: database3}'

Mapper和XML类名需要以Multi为前缀，如MultiMapper.java、MultiMapper.xml
不是以Multi为前缀的Mapper作为普通Mapper，不进行多数据库配置
mapper xml配置文件需要添加 @database 标识，如 @database.`table`，插件将 @database 替换为相关数据库名

使用方式可以参照example中的代码
