# mybatis-multi-database
springboot mybatis多数据库（实例）配置扩展插件

如果你的系统有多个项目的数据库实例都在一个数据库系统中（数据源），并且所需要的功能类型。如果你也在用MyBatis，建议尝试该插件，这一定能节省大量的工作。

springboot mybatis多数据库实例扩展插件支持通过一个基础通用Mapper，通过配置创建不同项目的Mapper，操作在同一个数据库系统中不同项目的数据库实例，避免创建多个项目相同的Mapper文件。
