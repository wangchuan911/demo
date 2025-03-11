package org.welisdoon.metadata.prototype.consts;

import org.apache.ibatis.session.SqlSessionFactory;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.dao.*;
import org.welisdoon.metadata.prototype.define.*;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.text.MessageFormat;
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

    static Map<Long, Enum<? extends IMetaType>> LONG_KEY_VALUE_MAP_2 = new HashMap<>();
    static Map<Class<? extends IMetaType>, Class<? extends MetaPrototypeCreator>> LONG_KEY_VALUE_MAP_1 = new HashMap<>();

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

    Reflections reflections;

    @Autowired
    public void setReflections(Reflections reflections, SqlSessionFactory sqlSessionFactory) {
        this.reflections = reflections;
        this.loadMetaType();

    }

    void loadMetaType() {
        reflections.getSubTypesOf(IMetaType.class).stream().filter(Class::isEnum).flatMap(aClass -> {
            return Arrays.stream(aClass.getEnumConstants());
        }).forEach(iMetaType -> {
            if ("UNKNOWN".equalsIgnoreCase(((Enum) iMetaType).name())) {
                return;
            }
            IMetaType metaType = (IMetaType) LONG_KEY_VALUE_MAP_2.put(iMetaType.getId(), (Enum) iMetaType);
            Assert.isNull(metaType, () -> {
                throw new IllegalStateException(MessageFormat.format("字典值存在冲突{0}:{1}.{2},{3}.{4}",
                        ((IMetaType) iMetaType).getId(),
                        iMetaType.getClass().getCanonicalName(), ((Enum) iMetaType).name(),
                        metaType.getClass().getCanonicalName(), ((Enum) metaType).name()));
            });
        });
        reflections.getSubTypesOf(ITypeEntity.class).stream().forEach(aClass -> {
            Arrays.stream(ApplicationContextProvider.getRawType(aClass, ITypeEntity.class)).forEach(type -> {
                reflections.getSubTypesOf(MetaPrototypeCreator.class).stream().filter(aClass1 -> {
                    return Arrays.stream(ApplicationContextProvider.getRawType(aClass1, MetaPrototypeCreator.class)).anyMatch(type1 -> {
                        return type1 == aClass;
                    });
                }).forEach(aClass1 -> {
                    LONG_KEY_VALUE_MAP_1.put((Class) type, aClass1);
                });
            });
        });
    }

    /*public MetaPrototype getType(@NonNull MetaPrototype o) {
        MetaPrototype metaPrototype = getType(o.getTypeId());
        if (Objects.isNull(metaPrototype)) {
            return o;
        }
        metaPrototype.copyTo(o);
        return metaPrototype;
    }
*/
    public MetaPrototype getType(@NonNull Long typeId) {
        IMetaType iMetaType = getMetaType(typeId);
        return Optional.ofNullable(LONG_KEY_VALUE_MAP_1.get(iMetaType.getClass())).map(aClass -> {
            try {
                return ApplicationContextProvider.getApplicationContext().getBean(aClass).generate(typeId);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }).orElse(null);
    }

    public IMetaType getMetaType(@NonNull Long typeId) {
        if (LONG_KEY_VALUE_MAP_2.containsKey(typeId))
            return (IMetaType) LONG_KEY_VALUE_MAP_2.get(typeId);
        return null;
    }

    public <T extends MetaPrototype> T getType(@NonNull IMetaType iMetaType) {
        MetaPrototypeCreator metaPrototypeCreator = ApplicationContextProvider.getApplicationContext().getBean(LONG_KEY_VALUE_MAP_1.get(iMetaType.getClass()));
        return (T) metaPrototypeCreator.generate(iMetaType.getId());
    }

    public <T extends MetaObject> T getObject(@NonNull Long id) {
        return (T) metaObjectDao.get(id);
    }

    public <T extends MetaObject.Attribute> T getAttribute(@NonNull Long id) {
        return (T) metaAttributeDao.get(id);
    }

    public <T extends MetaValue> T getValue(@NonNull Long id) {
        MetaValue keyValue = metaValueDao.get(id);
        return Objects.isNull(keyValue) ? null : (T) keyValue;
    }

    public List<MetaLink> getChildrenLinks(@NonNull Long parentId) {
        return parentId == null ? Collections.emptyList() : metaLinkDao.list(new MetaLinkCondition().setParentId(parentId));
    }


    public static MetaUtils getInstance() {
        return Optional.ofNullable(instance).orElseGet(() -> {
            return instance = ApplicationContextProvider.getBean(MetaUtils.class);
        });
    }

    public MetaObjectDao getMetaObjectDao() {
        return metaObjectDao;
    }

    public MetaLinkDao getMetaLinkDao() {
        return metaLinkDao;
    }

    public MetaAttributeDao getMetaAttributeDao() {
        return metaAttributeDao;
    }

    public MetaValueDao getMetaValueDao() {
        return metaValueDao;
    }
}
