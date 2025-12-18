package org.welisdoon.common;

import java.util.function.Consumer;
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

    class Wrapper<T> {
        final T target;

        Wrapper(T t) {
            this.target = t;
        }

        public CodeUtils.Wrapper<T> andThen(Consumer<T> consumer) {
            consumer.accept(target);
            return this;
        }

        public T getTarget() {
            return target;
        }
    }

    static <T> CodeUtils.Wrapper<T> of(T t) {
        return new CodeUtils.Wrapper<>(t);
    }
}
