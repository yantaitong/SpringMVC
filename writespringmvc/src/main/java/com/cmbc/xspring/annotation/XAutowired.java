package com.cmbc.xspring.annotation;


import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XAutowired {
    String value() default  "";
}
