package com.booxj.opensource.mine.fundation.internals;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 使用接口时需要 new 一个实现类
 * 当需要动态获取一个接口的实现类时，可以使用配合文件定义实现类的类名，及 java.util.ServiceLoader
 *
 * 使用方式：
 *      1. 创建一个接口文件
 *      2. 在resources资源目录下创建META-INF/services文件夹
 *      3. 在services文件夹中创建文件，以接口全名命名
 *      4. 创建接口实现类
 */
public class ServiceBootstrap {

    public static <S> S loadFirst(Class<S> clazz){
        Iterator<S> iterator = loadAll(clazz);
        if (!iterator.hasNext()){
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
                    clazz.getName()));
        }
        return iterator.next();
    }

    public static <S> Iterator<S> loadAll(Class<S> clazz) {
        ServiceLoader<S> loader = ServiceLoader.load(clazz);
        return loader.iterator();
    }

    public static void main(String[] args) {

    }
}
