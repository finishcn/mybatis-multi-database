package com.framework.multi.loader;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * java动态创建类加载器
 *
 * @author liyu
 */
public class DynamicClassLoader extends ClassLoader {

    private byte[] bytes;

    @Override
    protected Class<?> findClass(String name) {
        return defineClass(name, bytes, 0, bytes.length);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * 自定义fileManager
     */
    public static class ClassJavaFileManager extends ForwardingJavaFileManager {
        private ClassJavaFileObject classJavaFileObject;

        public ClassJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        public ClassJavaFileObject getClassJavaFileObject() {
            return classJavaFileObject;
        }

        /**
         * 这个方法一定要自定义
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            return (classJavaFileObject = new ClassJavaFileObject(className, kind));
        }
    }

    /**
     * 存储源文件
     */
    public static class StringObject extends SimpleJavaFileObject {
        private String content;

        public StringObject(URI uri, Kind kind, String content) {
            super(uri, kind);
            this.content = content;
        }

        //使JavaCompiler可以从content获取java源码
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return this.content;
        }
    }

    /**
     * class文件（不需要存到文件中）
     */
    public static class ClassJavaFileObject extends SimpleJavaFileObject {

        ByteArrayOutputStream outputStream;

        public ClassJavaFileObject(String className, Kind kind) {
            super(URI.create(className + kind.extension), kind);
            this.outputStream = new ByteArrayOutputStream();
        }

        @Override
        public OutputStream openOutputStream() {
            return this.outputStream;
        }

        public byte[] getBytes() {
            return this.outputStream.toByteArray();
        }
    }
}
