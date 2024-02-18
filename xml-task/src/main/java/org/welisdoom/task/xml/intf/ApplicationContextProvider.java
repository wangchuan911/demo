package org.welisdoom.task.xml.intf;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (ApplicationContextProvider.applicationContext == null)
            ApplicationContextProvider.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }


    public static Class<?> getType(String name) {
        return getApplicationContext().getType(name);
    }

    public static Class<?> getRealClass(Class cls) {
        return /*cls.getName().indexOf("EnhancerBySpringCGLIB") > 0*/isGCLIBProxy(cls) ? cls.getSuperclass() : cls;
    }

    public static boolean isGCLIBProxy(Class aClass) {
        return aClass.getName().indexOf("EnhancerBySpringCGLIB") > 0;
    }

    public static Type[] getRawType(Object bean, Class targetClass) {
        return Arrays
                .stream(getRealClass(bean.getClass())
                        .getGenericInterfaces())
                .filter(type -> {
                    if (type instanceof ParameterizedType) {
                        return ((ParameterizedType) type).getRawType() == targetClass;
                    }
                    return false;
                }).map(type -> ((ParameterizedType) type).getActualTypeArguments())
                .findFirst()
                .get();
    }
}
