package com.framework.autoconfigure.jdbc;

import org.springframework.cglib.proxy.InterfaceMaker;

import java.util.concurrent.ConcurrentHashMap;

public class MultiConfiguration {

    private static ConcurrentHashMap<String, Class> baseMapperClassMap = new ConcurrentHashMap();

    private static ConcurrentHashMap<String, Class> mapperClassMap = new ConcurrentHashMap();

    /**
     * 获取BaseMapper 类对象
     *
     * @param name
     * @return
     */
    public static Class getBaseMapperClass(String name) {
        Class clz = baseMapperClassMap.get(name);
        if (null == clz) {
            try {
                clz = Class.forName(name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            baseMapperClassMap.put(name, clz);
        }
        return clz;
    }

    public static Class getMapperClass(String beanName, String baseBeanName) {
        Class clz = mapperClassMap.get(beanName);
        if (null == clz) {
            Class baseClass = getBaseMapperClass(baseBeanName);
            InterfaceMaker im = new InterfaceMaker();
            im.add(baseClass);
            clz = im.create();
            mapperClassMap.put(beanName, clz);
            baseMapperClassMap.put(beanName, baseClass);
        }
        return clz;
    }

    public static Class getMapperClass(String beanName) {
        return mapperClassMap.get(beanName);
    }

    public static void remove(String beanName) {
        mapperClassMap.remove(beanName);
        baseMapperClassMap.remove(beanName);
    }

    public static void clear() {
        mapperClassMap.clear();
        mapperClassMap = null;
        mapperClassMap.clear();
        mapperClassMap = null;
    }
}
