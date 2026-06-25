package org.welisdoon.web.config.condition;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.welisdoon.common.object.wrapper.execute.BaseProxyProcessor;
import org.welisdoon.common.object.wrapper.execute.ExecuteWrapper;
import org.welisdoon.web.MySpringApplication;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

/**
 * @Classname ExecuteWrapperBeanProcessor
 * @Description TODO
 * @Author Septem
 * @Date 17:04
 */
@Component
public class BeanRemoveProcessor implements TypeFilter {
    List<Class<? extends Annotation>> annotations = List.of(SpringBootApplication.class, SpringBootConfiguration.class, EnableAutoConfiguration.class);

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        try {
            if (annotationMetadata.getAnnotationTypes().stream().noneMatch(s -> s.startsWith("org.springframework")))
                return false;

            if (annotationMetadata.getAnnotationTypes().stream().anyMatch(s -> annotations.stream().anyMatch(aClass -> Objects.equals(s, aClass.getName())))) {
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
