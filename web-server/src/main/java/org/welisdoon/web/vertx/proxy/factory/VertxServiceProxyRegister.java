package org.welisdoon.web.vertx.proxy.factory;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.welisdoon.web.vertx.annotation.VertxServiceProxy;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname VertxProxyRegister
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/16 16:24
 */
public class VertxServiceProxyRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(VertxServiceProxyScan.class.getName()));
        List<String> basePackages = new ArrayList();
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName).collect(Collectors.toList()));
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        VertxServiceProxyInterfacesScanner vertxServiceProxyInterfacesScanner = new VertxServiceProxyInterfacesScanner(registry);
        vertxServiceProxyInterfacesScanner.doScan(basePackages.stream().toArray(String[]::new));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

    }
}
