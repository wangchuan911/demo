package org.welisdoon.common;

import java.util.function.Supplier;

/**
 * @Classname CodeUtils
 * @Description TODO
 * @Author Septem
 * @Date 9:56
 */
public interface CodeUtils {
    static <T> T creator(Supplier<T> tSupplier) {
        return tSupplier.get();
    }

    @FunctionalInterface
    interface CodeBlock {
        void apply();
    }

    static void block(CodeBlock tSupplier) {
        tSupplier.apply();
    }
}
