package org.welisdoon.common.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.TypeUtils;
import org.reflections.Reflections;
import org.welisdoon.common.ObjectUtils;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface IData<ID> {
    ID getId();

    void setId(ID id);

    String getModelAlias();

    void setModelAlias(String modelAlias);

    abstract class DataObject<ID> implements IData<ID> {
        ID id;
        String modelAlias;

        public void setId(ID id) {
            this.id = id;
        }

        @Override
        public ID getId() {
            return id;
        }

        @Override
        public String getModelAlias() {
            return modelAlias == null || modelAlias.isEmpty() ? ObjectUtils.getAnnotations(this.getClass(), DataModelAlias.class)[0].value()[0] : this.modelAlias;
        }

        public void setModelAlias(String modelAlias) {
            this.modelAlias = modelAlias;
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(DataModelAliases.class)
    @interface DataModelAlias {
        String[] value();

        Class<?> processor() default Object.class;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface DataModelAliases {
        DataModelAlias[] value();

        Class<?> processor() default Object.class;
    }

    Map<String, Class<IData>> DATA_MAP = new HashMap<>();

    static void scan(Reflections reflections) {
        Stream
                .of(
                        reflections.getTypesAnnotatedWith(DataModelAlias.class),
                        reflections.getTypesAnnotatedWith(DataModelAliases.class)
                )
                .flatMap(objects -> objects.stream())
                .forEach(aClass -> {
                    if (!IData.class.isAssignableFrom(aClass)) return;
                    DataModelAlias[] dataModelAliases = ObjectUtils.getAnnotations(aClass, DataModelAlias.class);
                    for (DataModelAlias dataModelAlias : dataModelAliases) {
                        for (String s : dataModelAlias.value()) {
                            if (!DATA_MAP.containsKey(s) || DATA_MAP.get(s).isAssignableFrom(aClass)) {
                                DATA_MAP.put(s, (Class<IData>) aClass);
                            }
                        }
                    }
                });
    }

    static <T extends IData> T jsonToDataEntity(JSONObject object) {
        return (T) TypeUtils.castToJavaBean(object, DATA_MAP.get(object.getString("modelAlias")));
    }

    static <T extends IData> T jsonToDataEntity(String jsonString) {
        return jsonToDataEntity(JSONObject.parseObject(jsonString));
    }

}
