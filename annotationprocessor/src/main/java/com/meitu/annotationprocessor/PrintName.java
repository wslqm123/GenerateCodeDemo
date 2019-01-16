package com.meitu.annotationprocessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by LQM on 2018/11/24
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PrintName {
    String value() default "";
}
