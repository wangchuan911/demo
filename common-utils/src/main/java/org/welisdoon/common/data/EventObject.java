package org.welisdoon.common.data;

import org.welisdoon.common.ObjectUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * @Classname EventObject
 * @Description TODO
 * @Author Septem
 * @Date 17:26
 */
public interface EventObject<E extends Event, O extends EventObject> {

    Map<E, List<Consumer<O>>> getEventListMap();

    default void addEvent(E event, Consumer<O>... listener) {
        try {
            ObjectUtils.getMapValueOrNewSafe(getEventListMap(), event, () -> new LinkedList<>()).addAll(Arrays.asList(listener));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    default void removeEvent(E event, Consumer<? extends EventObject> listener) {
        if (!getEventListMap().containsKey(event)) return;
        getEventListMap().get(event).remove(listener);
    }

    default void removeEvent(E event) {
        getEventListMap().remove(event);
    }

    default void triggerEvent(E event) {
        Optional.ofNullable(getEventListMap().get(event)).orElseGet(LinkedList::new).stream().forEach(oConsumer -> {
            oConsumer.accept((O) this);
        });
    }
}
