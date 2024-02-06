package org.welisdoon.metadata.prototype.consts;

import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.dao.MetaAttributeDao;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.define.MetaPrototype;
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
    final static Logger logger = LoggerFactory.getLogger(MetaUtils.class);

    static Map<Long, KeyValue<Class<? extends MetaPrototype>, IMetaType>> LONG_KEY_VALUE_MAP = new HashMap<>();

    static KeyValue<Class<? extends MetaPrototype>, IMetaType> get(long id) {
        return LONG_KEY_VALUE_MAP.get(id);
    }

    static MetaAttributeDao metaAttributeDao;
    static MetaObjectDao metaObjectDao;
    static MetaLinkDao metaLinkDao;


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
                                if (Objects.nonNull(iMetaType)
                                        && (!LONG_KEY_VALUE_MAP.containsKey(iMetaType.getId()) || aClass1.isAssignableFrom(LONG_KEY_VALUE_MAP.get(iMetaType.getId()).getKey()))) {
                                    LONG_KEY_VALUE_MAP.put(iMetaType.getId(), new DefaultKeyValue(aClass1, iMetaType));
                                }
                            });
                });
        logger.info(LONG_KEY_VALUE_MAP.toString());

        metaAttributeDao = ApplicationContextProvider.getBean(MetaAttributeDao.class);
        metaObjectDao = ApplicationContextProvider.getBean(MetaObjectDao.class);
        metaLinkDao = ApplicationContextProvider.getBean(MetaLinkDao.class);
    }

    public static MetaPrototype getType(@NonNull MetaPrototype o) {
        return TypeUtils.castToJavaBean(o, LONG_KEY_VALUE_MAP.get(o.getTypeId()).getKey());
    }

    public static MetaPrototype getType(@NonNull Long typeId) {
        return TypeUtils.castToJavaBean(MapUtils.EMPTY_SORTED_MAP, LONG_KEY_VALUE_MAP.get(typeId).getKey());
    }

    public static MetaPrototype getType(@NonNull IMetaType iMetaType) {
        return getType(iMetaType.getId());
    }

    public static MetaObject getObject(@NonNull Long id) {

        MetaObject metaObject = metaObjectDao.get(id);
        return Objects.isNull(metaObject) ? null : (MetaObject) getType(metaObject);
    }

    public static MetaObject.Attribute getAttribute(@NonNull Long id) {
        MetaObject.Attribute metaAttribute = metaAttributeDao.get(id);
        return Objects.isNull(metaAttribute) ? null : (MetaObject.Attribute) getType(metaAttribute);
    }
}
