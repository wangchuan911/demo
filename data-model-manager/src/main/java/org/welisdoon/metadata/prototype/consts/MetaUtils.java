package org.welisdoon.metadata.prototype.consts;

import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.dao.MetaAttributeDao;
import org.welisdoon.metadata.prototype.dao.MetaValueDao;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.MetaValue;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.define.MetaPrototype;
import org.welisdoon.web.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.*;

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


    MetaAttributeDao metaAttributeDao;
    MetaObjectDao metaObjectDao;
    MetaLinkDao metaLinkDao;
    MetaValueDao metaValueDao;
    static MetaUtils instance;

    @Autowired
    public void setMetaObjectDao(MetaObjectDao metaObjectDao) {
        this.metaObjectDao = metaObjectDao;
    }

    @Autowired
    public void setMetaAttributeDao(MetaAttributeDao metaAttributeDao) {
        this.metaAttributeDao = metaAttributeDao;
    }

    @Autowired
    public void setMetaLinkDao(MetaLinkDao metaLinkDao) {
        this.metaLinkDao = metaLinkDao;
    }

    @Autowired
    public void setMetaKeyValueDao(MetaValueDao metaValueDao) {
        this.metaValueDao = metaValueDao;
    }

    @Autowired
    Reflections reflections;

    @PostConstruct
    void init() {
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
    }

    public MetaPrototype getType(@NonNull MetaPrototype o) {
        MetaPrototype metaPrototype = getType(o.getTypeId());
        if (Objects.isNull(metaPrototype)) {
            return o;
        }
        metaPrototype.copyTo(o);
        return metaPrototype;
    }

    public MetaPrototype getType(@NonNull Long typeId) {
        if (LONG_KEY_VALUE_MAP.containsKey(typeId))
            return TypeUtils.castToJavaBean(MapUtils.EMPTY_SORTED_MAP, LONG_KEY_VALUE_MAP.get(typeId).getKey());
        return null;
    }

    public MetaPrototype getType(@NonNull IMetaType iMetaType) {
        return getType(iMetaType.getId());
    }

    public MetaObject getObject(@NonNull Long id) {

        MetaObject metaObject = metaObjectDao.get(id);
        return Objects.isNull(metaObject) ? null : (MetaObject) getType(metaObject);
    }

    public MetaObject.Attribute getAttribute(@NonNull Long id) {
        MetaObject.Attribute metaAttribute = metaAttributeDao.get(id);
        return Objects.isNull(metaAttribute) ? null : (MetaObject.Attribute) getType(metaAttribute);
    }

    public MetaValue getValue(@NonNull Long id) {
        MetaValue keyValue = metaValueDao.get(id);
        return Objects.isNull(keyValue) ? null : keyValue;
    }

    public List<MetaLink> getChildrenLinks(@NonNull Long parentId) {
        return metaLinkDao.list(new MetaLinkCondition().setParentId(parentId));
    }


    public static MetaUtils getInstance() {
        return Optional.ofNullable(instance).orElseGet(() -> {
            return instance = ApplicationContextProvider.getBean(MetaUtils.class);
        });
    }
}
