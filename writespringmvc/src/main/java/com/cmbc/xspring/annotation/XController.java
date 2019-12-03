package com.cmbc.xspring.annotation;


import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.*;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XController {
    String value()default "";
}
