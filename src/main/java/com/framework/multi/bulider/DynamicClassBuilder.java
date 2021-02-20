package com.framework.multi.bulider;

import com.framework.multi.loader.DynamicClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URI;
import java.util.Arrays;

/**
 * java动态创建类
 *
 * @author liyu
 */
public class DynamicClassBuilder {

    /**
     * 创建mapper类字符串
     */
    public static Class<?> createMapperClass(DynamicClassLoader classLoader, String className, String baseMapperClass) throws Exception {
        String pg = baseMapperClass.substring(0, baseMapperClass.lastIndexOf("."));
        String classString = "package " + pg + "; public interface " + className + " extends " + baseMapperClass + "{}";
        return createClass(classLoader, pg + "." + className, classString);
    }

    /**
     * 创建类class
     */
    public static Class<?> createClass(DynamicClassLoader classLoader, String className, String classString) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        DynamicClassLoader.ClassJavaFileManager classJavaFileManager = new DynamicClassLoader.ClassJavaFileManager(standardFileManager);
        DynamicClassLoader.StringObject stringObject = new DynamicClassLoader.StringObject(
                new URI("/" + className.replace(".", "/") + ".java"), JavaFileObject.Kind.SOURCE, classString);
        JavaCompiler.CompilationTask task = compiler.getTask(null, classJavaFileManager, null, null, null,
                Arrays.asList(stringObject));
        if (task.call()) {
            DynamicClassLoader.ClassJavaFileObject javaFileObject = classJavaFileManager.getClassJavaFileObject();
            classLoader.setBytes(javaFileObject.getBytes());
            return classLoader.loadClass(className);
        }
        return null;
    }
}
