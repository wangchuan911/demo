package org.welisdoon.common.object.wrapper.execute;

import com.sun.jdi.ClassType;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Classname SpringProxyProcessor
 * @Description TODO
 * @Author Septem
 * @Date 14:28
 */
public class SpringProxyProcessor extends BaseProxyProcessor {
    final String interfaces;
    final String annotations;
    static final Pattern key = Pattern.compile("(value\\=\")(\\w*)(\")");


    public SpringProxyProcessor(Class<?> aClass) {
        super(aClass);
        this.interfaces = Arrays.stream(aClass.getInterfaces()).map(Class::getName).collect(Collectors.joining(","));
        StringBuilder builder = new StringBuilder();
        for (Annotation annotation : aClass.getAnnotations()) {
            if (annotation.toString().contains(ExecuteWrapper.class.getName())) continue;
            try {
                boolean isSpringScanAnnotationAndNaming = annotation.annotationType().getName().startsWith("org.springframework") && ("org.springframework.stereotype.Component".equals(annotation.annotationType().getName())
                        || Arrays.stream(annotation.annotationType().getMethods())
                        .filter(method -> "value".equals(method.getName()) && method.getReturnType() == String.class)
                        .anyMatch(method -> {
                            return Arrays.stream(method.getAnnotations()).map(Annotation::toString).anyMatch(s -> {
                                return s.contains("@org.springframework.core.annotation.AliasFor") && s.contains("annotation=org.springframework.stereotype.Component");
                            });
                        }));
                builder.append("\n");
                if (isSpringScanAnnotationAndNaming) {
                    Matcher matcher = key.matcher(annotation.toString());
                    matcher.find();
                    if (StringUtils.isEmpty(matcher.group(2))) {
                        builder.append(matcher.replaceAll("$1" + getBeanName() + "$3"));
                    }
                } else
                    builder.append(annotation.toString());

            } catch (Throwable e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        this.annotations = builder.toString();
    }

    public static Object toAnnotationVal(Object o) {
        if (o instanceof Class) {
            return ((Class<?>) o).getName() + ".class";
        }
        if (o instanceof ClassType) {
            return o.toString() + ".class";
        }
        if (o instanceof CharSequence) {
            return "\"" + o.toString() + "\"";
        }
        return o;
    }

    public SpringProxyProcessor(TypeElement target) {
        super(target);
        this.interfaces = target.getInterfaces().stream().map(interfaceElement -> ((TypeElement) interfaceElement).getQualifiedName().toString()).collect(Collectors.joining(","));
        StringBuilder builder = new StringBuilder();

        for (AnnotationMirror annotationMirror : target.getAnnotationMirrors()) {
            String annotationType = annotationMirror.getAnnotationType().toString();
            if (ExecuteWrapper.class.getName().equals(annotationType)) continue;
            try {
                boolean isSpringScanAnnotationAndNaming = annotationType.startsWith("org.springframework") && ("org.springframework.stereotype.Component".equals(annotationType) || annotationMirror.getAnnotationType().asElement().getEnclosedElements().stream().anyMatch(enclosedElement -> {
                    return enclosedElement instanceof ExecutableElement
                            && "value".equals(enclosedElement.getSimpleName().toString())
                            && String.class.getName().equals(getReturnFullType((ExecutableElement) enclosedElement))
                            && enclosedElement.getAnnotationMirrors().stream().map(Object::toString)
                            .anyMatch(methodAnnotationString -> {
                                return methodAnnotationString.contains("@org.springframework.core.annotation.AliasFor") && methodAnnotationString.contains("annotation=org.springframework.stereotype.Component");
                            })
                            && (!annotationMirror.getElementValues().containsKey(enclosedElement)
                            || StringUtils.isEmpty((String) annotationMirror.getElementValues().get(enclosedElement).getValue()));
                }));
                builder.append("\n");
                if (isSpringScanAnnotationAndNaming) {
                    String annotation = annotationMirror.toString();
                    if (!annotation.contains("(") && !annotation.endsWith(")")) {
                        builder.append(annotation).append("(value=\"").append(getBeanName()).append("\")");
                        continue;
                    } else {
                        Matcher matcher = key.matcher(annotation);
                        if (matcher.find()) {
                            if (StringUtils.isEmpty(matcher.group(2))) {
                                builder.append(matcher.replaceAll("$1" + getBeanName() + "$3"));
                                continue;
                            }
                        } else {
                            builder.append(annotation);
                            builder.delete(builder.length() - 1, builder.length());
                            builder.append(",value=\"").append(getBeanName()).append("\")");
                            continue;
                        }
                    }
                }
                builder.append(annotationMirror.toString());

            } catch (Throwable e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        this.annotations = builder.toString();
    }

    protected String getBeanName() {
        int lastIndex = className.lastIndexOf(".");
        return className.substring(lastIndex + 1, lastIndex + 2).toLowerCase(Locale.ROOT) + className.substring(lastIndex + 2);
    }

    public void toJava(BiConsumer<String, String> stringStringBiConsumer) {
        Set<String> excludeMethod = new HashSet<>();

        StringBuilder builder1 = new StringBuilder("    public Object _apply001(String key, Object o, Object[] o1) throws Throwable { \n"), builder2 = new StringBuilder("    public Class<?>[] _apply002(String key) { \n");
        append("        ", builder1, builder2);
        String packageName = className.substring(0, className.lastIndexOf("."));
        append("switch(key) {\n", builder1, builder2);
        toJava(excludeMethod, builder1, builder2);
        append("            ", builder1, builder2);
        append("default:\n", builder1, builder2);
        append("                  ", builder1, builder2);
        append("throw new IllegalStateException(\"不支持的方法:" + getExecutorName(className) + "\"+key);\n", builder1, builder2);


        append("            ", builder1, builder2);
        append("}\n", builder1, builder2);
        append("      ", builder1, builder2);
        append("}\n", builder1, builder2);

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n");
        builder.append(annotations).append("\n");
        builder.append("public class ").append(getExecutorName(className, false));
        builder.append(" extends ").append(className);
        builder.append(" implements org.welisdoon.common.object.wrapper.execute.Executor");
        if (StringUtils.isNotEmpty(interfaces)) {
            builder.append(",").append(interfaces);
        }

        builder.append(" { \n").append(builder1).append("\n").append(builder2).append("}\n");

        stringStringBiConsumer.accept(getExecutorName(className), builder.toString());
    }

    protected void toJava(Set<String> methods2, StringBuilder... builders) {
        if (parent != null) {
            ((SpringProxyProcessor) parent).toJava(methods2, builders);
        }
        for (Map.Entry<String, String[]> entry : methods) {
            toJava(entry, methods2, builders);
        }
    }

    protected void toJava(Map.Entry<String, String[]> entry, Set<String> methods, StringBuilder... builders) {
        StringBuilder builder1 = builders[0], builder2 = builders[1];
        String methodName = entry.getKey();
        List<String> parameterTypes = Arrays.asList(entry.getValue()).subList(0, entry.getValue().length - 1);
        String returnTypes = entry.getValue()[entry.getValue().length - 1];
        String key = toKey(methodName, parameterTypes.toString());
        if (methods.contains(key)) return;
        methods.add(key);
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

    public BaseProxyProcessor newInstance(TypeElement target) {
        BaseProxyProcessor baseProxyProcessor = new SpringProxyProcessor(target);
        CACHE.add(baseProxyProcessor);
        return baseProxyProcessor;
    }

    public BaseProxyProcessor newInstance(Class<?> target) {
        BaseProxyProcessor baseProxyProcessor = new SpringProxyProcessor(target);
        CACHE.add(baseProxyProcessor);
        return baseProxyProcessor;
    }
}
