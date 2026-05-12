package org.welisdoon.web.config.condition;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.welisdoon.common.object.wrapper.execute.BaseProxyProcessor;
import org.welisdoon.common.object.wrapper.execute.ExecuteWrapper;
import org.welisdoon.common.object.wrapper.execute.ExecuteWrapperProcessor;
import org.welisdoon.web.MySpringApplication;
import org.welisdoon.web.vertx.proxy.meta.ClassData;

import java.io.IOException;

/**
 * @Classname ExecuteWrapperBeanProcessor
 * @Description TODO
 * @Author Septem
 * @Date 17:04
 */
@Component
public class BeanRemoveProcessor implements TypeFilter {

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        try {
            if (annotationMetadata.getAnnotationTypes().stream().noneMatch(s -> s.startsWith("org.springframework")))
                return false;

            if (annotationMetadata.getAnnotationTypes().contains(SpringBootApplication.class.getName())) {
                return !MySpringApplication.getAppClass().getName().equals(classMetadata.getClassName());
            }

            if (annotationMetadata.getAnnotationTypes().contains(ExecuteWrapper.class.getName())) {
                Class.forName(BaseProxyProcessor.getExecutorName(classMetadata.getClassName()));
                return true;
            }
        } catch (Throwable e) {
            System.out.println(classMetadata.getClassName());
            e.printStackTrace();
        }
        return false;
    }
}
