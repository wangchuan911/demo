package org.welisdoon.metadata.prototype.consts;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.reflections.Reflections;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname MetaUtils
 * @Description TODO
 * @Author Septem
 * @Date 11:29
 */

@Component
public class MetaUtils {
    static Map<Long, KeyValue<Class<? extends MetaObject>, IMetaType>> LONG_KEY_VALUE_MAP = new HashMap<>();

    static KeyValue<Class<? extends MetaObject>, IMetaType> get(long id) {
        return LONG_KEY_VALUE_MAP.get(id);
    }

    @EventListener
    void init(ApplicationReadyEvent event) {
        Reflections reflections = ApplicationContextProvider.getApplicationContext().getBean(Reflections.class);
        reflections.getTypesAnnotatedWith(Meta.class).stream()
                .filter(aClass -> Annotation.class.isAssignableFrom(aClass))
                .map(aClass -> (Class<? extends Annotation>) aClass)
                .forEach(aClass -> {
                    reflections.getTypesAnnotatedWith(aClass)
                            .stream()
                            .filter(aClass1 -> MetaObject.class.isAssignableFrom(aClass1))
                            .forEach(aClass1 -> {
                                IMetaType iMetaType = ((IMetaType) AnnotationUtils.getValue(aClass1.getAnnotation(aClass)));
                                if (Objects.nonNull(iMetaType)) {
                                    LONG_KEY_VALUE_MAP.put(iMetaType.getId(), new DefaultKeyValue(aClass1, iMetaType));
                                }
                            });
                });
        System.out.println(LONG_KEY_VALUE_MAP);
    }
}
