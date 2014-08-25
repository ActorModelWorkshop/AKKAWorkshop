package com.endava.actormodel.akka.base.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class SpringContext {

    private static SpringContext instance;

    private static final String SPRING_CONFIG_PACKAGE = "com.endava.actormodel.akka.base.config";

    private final GenericApplicationContext applicationContext;

    public static SpringContext initialize() {
        if (null == instance) {
            instance = new SpringContext();
        }

        return instance;
    }

    public static <Type> Type getBean(final String beanName) {
        if (null == instance) {
            return null;
        }

        return (Type) instance.applicationContext.getBean(beanName);
    }

    private SpringContext() {
        applicationContext = new AnnotationConfigApplicationContext();

        ((AnnotationConfigApplicationContext) applicationContext).scan(SPRING_CONFIG_PACKAGE);
        applicationContext.refresh();
        applicationContext.start();
    }
}
