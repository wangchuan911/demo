package org.welisdoon.web.vertx.proxy.meta;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.object.wrapper.execute.BaseProxyProcessor;
import org.welisdoon.common.object.wrapper.execute.Executor;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.service.wechat.service.WeChatService;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname ClassData
 * @Description TODO
 * @Author Septem
 * @Date 10:52
 */

@DataObject
public class ClassData {
    final String className;
    final static Map<ClassData, ClassData> CACHE = new HashMap<>();
    final static Map<String, Class<?>> CACHE2 = new HashMap<>();
    /*Executor executor;*/

    Class<?> target;

    public String getClassName() {
        return className;
    }

    public Class<?> getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassData classData = (ClassData) o;
        return Objects.equals(className, classData.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    public JsonObject toJson() {
        return null;
    }

    public void build() {
        try {
            this.target = getClass(className);
            /*this.executor = buildExecutor(this.target);*/
        } catch (Throwable e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static void append(String path, StringBuilder... builder) {
        for (StringBuilder stringBuilder : builder) {
            stringBuilder.append(path);
        }
    }

    public static String getExecutorName(Class<?> target) {
        return getExecutorName(target, true);
    }

    public static String getExecutorName(Class<?> target, boolean showPackage) {
        return BaseProxyProcessor.getExecutorName(showPackage ? target.getName() : target.getSimpleName());
    }


   /* public synchronized static Executor buildExecutor(Class<?> target) {
        if (CACHE.containsKey(new ClassData(target))) return CACHE.get(new ClassData(target)).executor;
        Executor executor;
        try {
            executor = (Executor) getClass(getExecutorName(target)).getConstructor().newInstance();
        } catch (Throwable e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return executor;
    }

    public static String toJava(Class<?> target) {
        boolean hasSupper = target.getSuperclass() != Object.class;
        if (hasSupper) {
            toJava(target.getSuperclass());
        }
        StringBuilder builder1 = new StringBuilder("    public Object apply(String key, Object o, Object[] o1) { \n"), builder2 = new StringBuilder("    public Class<?>[] apply(String key) { \n");
        append("        ", builder1, builder2);
        append("switch(key) {\n", builder1, builder2);
        for (Method method : target.getMethods()) {
            String key = new MethodData(method).toKey();
            if (ExecuteWrapperProcessor.excludeMethod.contains(key)) continue;
            if (method.getModifiers() != Modifier.PUBLIC) continue;
            append("            ", builder1, builder2);
            append("case \"", builder1, builder2);
            append(key, builder1, builder2);
            append("\":\n", builder1, builder2);
            append("                  ", builder1, builder2);
            builder2.append("return new java.lang.Class[]{");
            boolean isVoid = method.getReturnType().getSimpleName().equals("void");
            if (!isVoid) {
                builder1.append("return ");
            }
            builder1.append("((").append(target.getName()).append(")o).").append(method.getName()).append("(");
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                builder1.append("(").append(parameterType.getName()).append(")o1[").append(i).append("]").append(i == parameterTypes.length - 1 ? "" : ",");
                builder2.append(parameterType.getName()).append(".class,");
            }
            builder1.append(");\n");
            if (isVoid) {
                builder1.append("                  return null;\n");
            }
            builder2.append(method.getReturnType().getName()).append(".class").append("};\n");
        }
        append("            ", builder1, builder2);
        append("default:\n", builder1, builder2);
        append("                  ", builder1, builder2);
        if (hasSupper) {
            append("return super.apply(key", builder1, builder2);
            builder1.append(",o,o1");
            append(");\n", builder1, builder2);
        } else {
            append("throw new IllegalStateException(\"不支持的方法:" + getExecutorName(target) + "\"+key);", builder1, builder2);
        }

        append("            ", builder1, builder2);
        append("}\n", builder1, builder2);
        append("      ", builder1, builder2);
        append("}\n", builder1, builder2);

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(target.getPackageName()).append(";\n");
        builder.append("public class ").append(getExecutorName(target, false));
        if (hasSupper) {
            builder.append(" extends ").append(getExecutorName(target.getSuperclass()));
        } else {
            builder.append(" implements org.welisdoon.common.object.wrapper.execute.Executor");
        }
        builder.append(" { \n").append(builder1).append("\n").append(builder2).append("}\n");
        return builder.toString();
    }*/

    /*public static void main(String[] args) {
        buildExecutor(WeChatService.class);
    }*/

    public Object getBean() {
        return ApplicationContextProvider.getBean(target);
    }

    public static ClassData getData(ClassData classData) throws InvocationTargetException {
        return ObjectUtils.getMapValueOrNewSafe(CACHE, classData, () -> {
            classData.build();
            return classData;
        });
    }

    public ClassData(Class<?> targetClass) {
        this.className = targetClass.getTypeName();
    }

    public ClassData(JsonObject jsonObject) {
        this.className = jsonObject.getString("clz");
    }

    public static Class getClass(String name) {
        try {
            return ObjectUtils.getMapValueOrNewSafe(CACHE2, name, () -> {
                return Class.forName(name);
            });
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Executor getExecutor() {
        return (Executor) ApplicationContextProvider.getBean(target);
    }
}
