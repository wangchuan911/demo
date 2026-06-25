package org.welisdoon.common.object.wrapper.execute;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname SpringProxyProcessor
 * @Description TODO
 * @Author Septem
 * @Date 14:28
 */
public class BaseProxyProcessor {
    static List<BaseProxyProcessor> CACHE = new LinkedList<>();
    final String className;
    final List<Map.Entry<String, String[]>> methods;
    final BaseProxyProcessor parent;
    static BiConsumer<String, Object[]> logger;

    public static void setLogger(BiConsumer<String, Object[]> logger) {
        BaseProxyProcessor.logger = logger;
    }

    protected static void log(String template, Object... strings) {
        if (logger != null)
            logger.accept(template, strings);
        else
            System.out.println(MessageFormat.format(template, strings));
    }

    public final static List<String> excludeMethod = Arrays.asList("wait[\"long\"]", "wait[\"long\",\"int\"]", "wait[]", "equals[\"java.lang.Object\"]", "toString[]", "hashCode[]", "getClass[]", "notify[]", "notifyAll[]");


    public BaseProxyProcessor(Class<?> aClass) {
        this.className = aClass.getName();
        List<Map.Entry<String, String[]>> list = new LinkedList<>();
        for (Method method : aClass.getMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) continue;
            String[] strings = new String[method.getParameterCount() + 1];
            Class<?>[] params = method.getParameterTypes();
            int i = 0;
            for (; i < params.length; i++) {
                strings[i] = params.getClass().getName();
            }
            strings[i] = method.getReturnType().getName();
            if (excludeMethod.contains(methodKey(method))) continue;
            list.add(Map.entry(method.getName(), strings));
        }
        this.methods = List.of(list.toArray(new Map.Entry[0]));
        Class<?> superClass = aClass.getSuperclass();

        this.parent = (superClass == null || superClass == Object.class) ? null : Optional.ofNullable(getCache(superClass.getName(), this.getClass())).orElseGet(() -> {
            return newInstance(superClass);
        });
    }

    public BaseProxyProcessor(TypeElement target) {
        this.className = target.getQualifiedName().toString();
        List<Map.Entry<String, String[]>> list = new LinkedList<>();

        for (Element element : target.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            if (!method.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC)) continue;
            String methodName = method.getSimpleName().toString();
            List<String> parameterTypes = method.getParameters().stream().map(BaseProxyProcessor::getTypeFullName).collect(Collectors.toList());
            String returnTypes = getReturnFullType(method);
            if (excludeMethod.contains(methodKey(method))) continue;
            String[] strings = new String[parameterTypes.size() + 1];
            parameterTypes.toArray(strings);
            strings[parameterTypes.size()] = returnTypes;
            list.add(Map.entry(methodName, strings));
        }
        this.methods = List.of(list.toArray(new Map.Entry[0]));

        TypeElement superClass = Optional.ofNullable(target.getSuperclass()).filter(typeMirror -> typeMirror.getKind() != TypeKind.NONE).map(typeMirror -> (TypeElement) ((DeclaredType) typeMirror).asElement()).filter(typeElement -> !typeElement.getQualifiedName().toString().equals(Object.class.getName())).orElse(null);

        this.parent = superClass == null ? null : Optional.ofNullable(getCache(superClass.getQualifiedName().toString(), this.getClass())).orElseGet(() -> {
            return newInstance(superClass);
        });
    }


    public BaseProxyProcessor newInstance(TypeElement target) {
        BaseProxyProcessor baseProxyProcessor = new BaseProxyProcessor(target);
        CACHE.add(baseProxyProcessor);
        return baseProxyProcessor;
    }

    public BaseProxyProcessor newInstance(Class<?> target) {
        BaseProxyProcessor baseProxyProcessor = new BaseProxyProcessor(target);
        CACHE.add(baseProxyProcessor);
        return baseProxyProcessor;
    }

    public static BaseProxyProcessor getCache(String className, Class<? extends BaseProxyProcessor> aClass) {
        return CACHE.stream().filter(baseProxyProcessor -> baseProxyProcessor.getClass() == aClass && baseProxyProcessor.className.equals(className)).findFirst().orElse(null);
    }

    public static String methodKey(Method method) {
        return toKey(method.getName(), Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList()));
    }

    public static String methodKey(ExecutableElement method) {
        return toKey(method.getSimpleName().toString(), method.getParameters().stream().map(BaseProxyProcessor::getTypeFullName).collect(Collectors.toList()));
    }


    public static String toKey(String methodName, String parameterTypes) {
        return methodName + parameterTypes;
    }

    public static String toKey(String methodName, List<String> parameterTypes) {
        return toKey(methodName, parameterTypes.toString());
    }

    public static String getReturnFullType(ExecutableElement executable) {
        TypeMirror returnType = executable.getReturnType();
        TypeKind kind = returnType.getKind();

        // void 直接返回
        switch (kind) {
            case VOID:
                return "void";

            // 引用类型
            case DECLARED: {
                DeclaredType dt = (DeclaredType) returnType;
                TypeElement te = (TypeElement) dt.asElement();
                return te.getQualifiedName().toString();
            }

            // 数组
            case ARRAY: {
                ArrayType at = (ArrayType) returnType;
                return at.getComponentType() + "[]";
            }

            // 基本类型 / 其他
            default:
                return returnType.toString();
        }
    }

    public static String getTypeFullName(VariableElement variableElement) {
        TypeMirror type = variableElement.asType();
        return getTypeFullName(type);
    }

    public static String getTypeFullName(TypeMirror type) {
        switch (type.getKind()) {
            // 基本类型
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return type.toString();

            // 引用类型、泛型
            case DECLARED: {
                DeclaredType dt = (DeclaredType) type;
                TypeElement te = (TypeElement) dt.asElement();
                return te.getQualifiedName().toString();
            }
            // 数组
            case ARRAY: {
                ArrayType at = (ArrayType) type;
                String compName = getTypeFullName(at.getComponentType());
                return compName + "[]";
            }
            // void / 其他
            case VOID:
                return "void";
            default:
                return "unknown";
        }
    }


    public void toJava(BiConsumer<String, String> stringStringBiConsumer) {
        if (parent != null) {
            parent.toJava(stringStringBiConsumer);
        }
        StringBuilder builder1 = new StringBuilder("    public Object _apply001(String key, Object o, Object[] o1) throws Throwable { \n"), builder2 = new StringBuilder("    public Class<?>[] _apply002(String key) { \n");
        append("        ", builder1, builder2);

        String packageName = className.substring(0, className.lastIndexOf("."));
        append("switch(key) {\n", builder1, builder2);
        for (Map.Entry<String, String[]> entry : methods) {
            String methodName = entry.getKey();
            List<String> parameterTypes = Arrays.asList(entry.getValue()).subList(0, entry.getValue().length - 1);
            String returnTypes = entry.getValue()[entry.getValue().length - 1];
            String key = toKey(methodName, parameterTypes.toString());
            append("            ", builder1, builder2);
            append("case \"", builder1, builder2);
            append(key, builder1, builder2);
            append("\":\n", builder1, builder2);
            append("                  ", builder1, builder2);

            builder2.append("return new java.lang.Class[]{");
            boolean isVoid = returnTypes.equals("void");
            if (!isVoid) {
                builder1.append("return ");
            }
            builder1.append("((").append(className).append(")o).").append(methodName).append("(");
            for (int i = 0; i < parameterTypes.size(); i++) {
                String parameterType = parameterTypes.get(i);
                builder1.append("(").append(parameterType).append(")o1[").append(i).append("]").append(i == parameterTypes.size() - 1 ? "" : ",");
                builder2.append(parameterType).append(".class,");
            }
            builder1.append(");\n");
            if (isVoid) {
                builder1.append("                  return null;\n");
            }
            builder2.append(returnTypes).append(".class").append("};\n");
        }
        append("            ", builder1, builder2);
        append("default:\n", builder1, builder2);
        append("                  ", builder1, builder2);
        if (parent != null) {
            append("return super._apply(key", builder1, builder2);
            builder1.append(",o,o1");
            append(");\n", builder1, builder2);
        } else {
            append("throw new IllegalStateException(\"不支持的方法:" + getExecutorName(className) + "\"+key);\n", builder1, builder2);
        }

        append("            ", builder1, builder2);
        append("}\n", builder1, builder2);
        append("      ", builder1, builder2);
        append("}\n", builder1, builder2);

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n");
        builder.append("public class ").append(getExecutorName(className, false));
        if (parent != null) {
            builder.append(" extends ").append(getExecutorName(parent.className));
        } else {
            builder.append(" implements org.welisdoon.common.object.wrapper.execute.Executor");
        }
        builder.append(" { \n").append(builder1).append("\n").append(builder2).append("}\n");
        stringStringBiConsumer.accept(getExecutorName(className), builder.toString());
    }

    public static void append(String path, StringBuilder... builder) {
        for (StringBuilder stringBuilder : builder) {
            stringBuilder.append(path);
        }
    }

    public static String getExecutorName(TypeElement target) {
        return getExecutorName(target.getQualifiedName().toString(), true);
    }

    public static String getExecutorName(Class<?> target) {
        return getExecutorName(target.getName(), true);
    }

    public static String getExecutorName(String name, boolean showPackage) {
        if (!showPackage) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }
        return getExecutorName(name);
    }

    public static String getExecutorName(String className) {
        return className + "ProxyExecutor";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseProxyProcessor that = (BaseProxyProcessor) o;
        return Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }
}
