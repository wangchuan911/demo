package org.welisdoon.common.data;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import org.reflections.Reflections;
import org.welisdoon.common.ObjectUtils;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface IData<ID, DATA_MARKER extends Enum<? extends IData.IDataMarker>> {
    ID getId();

    IData setId(ID id);

    DATA_MARKER getDataMarker();

    IData setDataMarker(DATA_MARKER dataMarker);

    abstract class DataObject<ID, DATA_MARKER extends Enum<? extends IData.IDataMarker>> implements IData<ID, DATA_MARKER> {
        ID id;
        DATA_MARKER dataMarker;

        public DataObject<ID, DATA_MARKER> setId(ID id) {
            this.id = id;
            return this;
        }

        @Override
        public ID getId() {
            return id;
        }

        @Override
        public DATA_MARKER getDataMarker() {
            return dataMarker;
        }

        public DataObject<ID, DATA_MARKER> setDataMarker(DATA_MARKER dataMarker) {
            this.dataMarker = dataMarker;
            return this;
        }
    }

    interface IDataMarker {
    }


    Map<Class<? extends Enum<? extends IDataMarker>>, Map<Enum<? extends IDataMarker>, Class<? extends IData>>> DATA_MAP = new HashMap<>();


    static void add(Enum<? extends IDataMarker> marker, Class<? extends IData> dataType) {
        Map<Enum<? extends IDataMarker>, Class<? extends IData>> map = DATA_MAP.get(marker.getClass());
        if (map == null) {
            map = new HashMap<>();
            DATA_MAP.put((Class<Enum<? extends IDataMarker>>) marker.getClass(), map);
        }
        map.put(marker, dataType);
    }

    static void add(Enum<? extends IDataMarker> marker, IData data) {
        add(marker, data.getClass());
    }

    static void add(IData data) {
        add(data.getDataMarker(), data.getClass());
    }

    static <T extends IData> T jsonToDataEntity(Class<? extends Enum<? extends IDataMarker>> enumClass, JSONObject object) {
        return (T) TypeUtils.castToJavaBean(object, DATA_MAP.get(enumClass).get(Enum.valueOf((Class) enumClass, object.getString("dataMarker"))));
    }

    static <T extends IData> T jsonToDataEntity(Class<? extends Enum<? extends IDataMarker>> enumClass, String jsonString) {
        return jsonToDataEntity(enumClass, JSONObject.parseObject(jsonString));
    }

}
