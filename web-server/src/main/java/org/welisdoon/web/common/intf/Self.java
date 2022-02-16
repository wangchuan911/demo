package org.welisdoon.web.common.intf;

import org.welisdoon.web.common.ApplicationContextProvider;

/**
 * @Classname Self
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/28 15:06
 */
public interface Self<T> {

    default T self() {
        return (T) ApplicationContextProvider.getBean(ApplicationContextProvider.getRealClass(this.getClass()));
    }
}
