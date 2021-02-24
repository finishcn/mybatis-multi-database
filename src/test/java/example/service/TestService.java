package com.data.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class TestService {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private NormalMapper normalMapper;

    @Resource(name = "project1P2DataMapper")
    private MultiP2DataMapper p2DataMapper;

    public void test() {
        MultiP1DataMapper msMapper = context.getBean("project1" + P1DataMapper.name, MultiP1DataMapper.class);
    }
}
