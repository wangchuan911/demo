package org.welisdoon.common.data;

import com.alibaba.fastjson.JSONObject;
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

    String getDataMarker();

    void setDataMarker(String dataMarker);

    abstract class DataObject<ID> implements IData<ID> {
        ID id;
        String dataMarker;

        public void setId(ID id) {
            this.id = id;
        }

        @Override
        public ID getId() {
            return id;
        }

        @Override
        public String getDataMarker() {
            return dataMarker == null || dataMarker.isEmpty() ? ObjectUtils.getAnnotations(this.getClass(), DataMarker.class)[0].value()[0] : this.dataMarker;
        }

        public void setDataMarker(String dataMarker) {
            this.dataMarker = dataMarker;
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(DataMarkers.class)
    @interface DataMarker {
        String[] value();

        Class<?> processor() default Object.class;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface DataMarkers {
        DataMarker[] value();

        Class<?> processor() default Object.class;
    }

    Map<String, Class<IData>> DATA_MAP = new HashMap<>();

    static void scan(Reflections reflections) {
        Stream
                .of(
                        reflections.getTypesAnnotatedWith(DataMarker.class),
                        reflections.getTypesAnnotatedWith(DataMarkers.class)
                )
                .flatMap(objects -> objects.stream())
                .forEach(aClass -> {
                    if (!IData.class.isAssignableFrom(aClass)) return;
                    DataMarker[] dataMarkers = ObjectUtils.getAnnotations(aClass, DataMarker.class);
                    for (DataMarker dataMarker : dataMarkers) {
                        for (String s : dataMarker.value()) {
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
