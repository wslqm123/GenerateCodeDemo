package com.meitu.sample;

import android.annotation.TargetApi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * AnnotationSample
 * extra: 注解案例
 * Created by LQM<lqm1@meitu.com> on 2018/11/25 - 10:06 PM
 */
public class AnnotationSample {

    // Retention.Type = SOURCE
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    // Retention.Type = CLASS
    @TargetApi(21)
    private void tarSam() {
    }

    // Retention.Type = RUNTIME
    // Documented
    @Deprecated
    private void depMethod() {
    }


    // 自定义注解
    @MyRuntimennotation("my runtime annotation")
    private String mName = null;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface MyRuntimennotation {
        String value() default "";
    }


    public static void main(String[] args){
        try {
            // 获取要解析的类
            Class cls = Class.forName("com.meitu.sample.AnnotationSample");
            // 拿到所有Field
            Field[] declaredFields = cls.getDeclaredFields();
            for(Field field : declaredFields){
                // 获取Field上的注解
                MyRuntimennotation annotation = field.getAnnotation(MyRuntimennotation.class);
                if(annotation != null){
                    // 获取注解值
                    String value = annotation.value();
                    System.out.println(value);
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
