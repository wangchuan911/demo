package org.welisdoon.common;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname GCUtils
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/2/10 11:21
 */
public interface GCUtils {
    static <T extends Collection<?>> T release(Collection<?> objects) {
        return release(objects, false);
    }

    static <T extends Collection<?>> T release(Collection<?> objects, boolean releaseElement) {
        if (releaseElement)
            objects.stream().forEach(o -> {
                release(o);
            });
        objects.clear();
        new SoftReference(objects);
        return (T) null;
    }

    static <T> T[] release(T[] objects) {
        return release(objects, false);
    }

    static <T> T[] release(T[] objects, boolean releaseElement) {
        for (int i = 0; i < objects.length; i++) {
            if (releaseElement) {
                release(objects[i]);
            }
            objects[i] = null;
        }
        new SoftReference(objects);
        return (T[]) null;
    }

    static <T> T release(T objects) {
        new SoftReference(objects);
        return (T) null;
    }

    static boolean toSafePoint() {
        try {
            Thread.sleep(0);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean toSafePointOnLoop(int count, int triggerCount) {
        if (count % triggerCount == 0) {
            return GCUtils.toSafePoint();
        }
        return true;
    }
}
