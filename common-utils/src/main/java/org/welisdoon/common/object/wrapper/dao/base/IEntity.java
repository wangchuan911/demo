package org.welisdoon.common.object.wrapper.dao.base;

import java.util.Map;

/**
 * @Classname BaseEntity
 * @Description TODO
 * @Author Septem
 * @Date 15:48
 */
public interface IEntity<T> {
    default Map<String, Object> data() {
        return Map.of();
    }

    default T getId() {
        return (T) data().get("id");
    }

    default String getDescription() {
        return "";
    }

    interface BuildInfo {

    }
}
