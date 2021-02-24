package com.example.p1.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper和XML类名需要以Multi为前缀
 * 不带Multi前缀的Mapper被认为是普通Mapper，不进行多数据库实例配置
 */
@Mapper
public interface MultiP1DataMapper {

    String name = "P1DataMapper";

    String getById(Integer id);
}
