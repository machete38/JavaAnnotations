package com.machete3845.java_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface LogMethod{
    enum Level {
        INFO, DEBUG, ERROR
    }
    Level level() default Level.INFO;
}


