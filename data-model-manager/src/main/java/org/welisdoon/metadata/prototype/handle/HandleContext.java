package org.welisdoon.metadata.prototype.handle;

import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.handle.link.LinkHandle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @Classname HandleParamater
 * @Description TODO
 * @Author Septem
 * @Date 18:15
 */
public class HandleContext {
    Map<LinkHandle, Object> map = new HashMap<>();

    public <T> T get(LinkHandle linkHandle) {
        return (T) map.get(linkHandle);
    }

    public <T> T get(LinkHandle linkHandle, Supplier<T> supplier) {
        try {
            return (T) ObjectUtils.getMapValueOrNewSafe(map, linkHandle, supplier::get);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable.getMessage(), throwable);
        }
    }

    public <T> T set(LinkHandle linkHandle, Supplier<T> supplier) {
        T t = supplier.get();
        map.put(linkHandle, t);
        return t;
    }
}
