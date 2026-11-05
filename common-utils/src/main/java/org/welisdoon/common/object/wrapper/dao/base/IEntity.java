package org.welisdoon.common.object.wrapper.dao.base;

import org.welisdoon.common.object.wrapper.database.SqlBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname BaseEntity
 * @Description TODO
 * @Author Septem
 * @Date 15:48
 */
public interface IEntity<T> {
    Map<String, Object> data();

    default T id() {
        return (T) data().get("id");
    }

    default <T> T value(String key) {
        String prefix;
        String suffix;
        if (key.contains(".")) {
            int idx = key.indexOf(".");
            prefix = key.substring(0, idx);
            suffix = key.substring(idx + 1);
        } else {
            prefix = key;
            suffix = key;
        }
        Object o = data().get(prefix);
        if (o instanceof SqlBuilder.SelectGroup.Query) {
            ((SqlBuilder.SelectGroup.Query) o).load(suffix, data());
            o = data().get(prefix);
        }
        if (o == null) return null;
        if (!key.contains(".")) return (T) o;
        if (o instanceof IEntity) return ((IEntity<?>) o).value(suffix);
        if (o instanceof List)
            return (T) ((List<?>) o).stream().map(o1 -> ((IEntity) o1).value(suffix)).collect(Collectors.toList());

        throw new IllegalStateException(String.format("不能获取%s的值，上级对象%s", key, o));
    }

    default String getDescription() {
        return "";
    }

    interface BuildInfo {

    }
}
