package org.welisdoon.common.object.wrapper.execute;

import com.google.auto.service.AutoService;
import com.google.common.base.Throwables;
import com.sun.jdi.ClassType;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Classname CodeWrapper
 * @Description TODO
 * @Author Septem
 * @Date 20:59
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("org.welisdoon.common.object.wrapper.execute.ExecuteWrapper")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ExecuteWrapperProcessor extends AbstractProcessor {

    public void log(Diagnostic.Kind kind, String template, Object... objects) {
        processingEnv.getMessager().printMessage(kind, MessageFormat.format("{0} {1}", this.getClass().getName(), MessageFormat.format(template, objects)));
    }
    /*final static boolean IS_WINDOWS = Optional.ofNullable(System.getProperty("os.name")).filter(s -> s.toLowerCase().contains("windows")).isPresent();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    protected String getSimpleName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }


    static class Source {
        String className, java, sourceJava, enhanceJava;

        Source(String path, String source, Element element) {
            className = ((TypeElement) element).getQualifiedName().toString();
            java = className.replaceAll("\\.", "/") + ".java";
            sourceJava = source + java;
            enhanceJava = path + java;
        }

        Path getSource() {
            return Paths.get(IS_WINDOWS && sourceJava.startsWith("/") ? sourceJava.substring(1) : sourceJava);
        }

        FileObject getTarget(Filer filer) throws IOException {
            return filer.createResource(StandardLocation.SOURCE_OUTPUT, className.substring(0, className.lastIndexOf(".")), className.substring(className.lastIndexOf(".") + 1));
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(IDataAccessObject.DataFuture.class);
            if (CollectionUtils.isEmpty(set)) return true;

            String path = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "", "").toUri().getPath() + "/";
            log(Diagnostic.Kind.NOTE, path);
            String source = path.substring(0, path.indexOf("/target/")) + "/src/main/java/";
            log(Diagnostic.Kind.NOTE, source);
            Source source1;
            for (Element element : set) {
                source1 = new Source(path, source, element);
                log(Diagnostic.Kind.NOTE, "---------------------\nSource:{0}\nEnhance :{1}", source1.sourceJava, source1.enhanceJava);
//                log(Diagnostic.Kind.NOTE, "{0}", Files.readString(Paths.get(isWindows && sourceJava.startsWith("/") ? sourceJava.substring(1) : sourceJava)));
                modify(source1.getSource(), source1.getTarget(processingEnv.getFiler()));
            }

        } catch (Throwable e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

    protected void modify(Path source, FileObject fileObject) throws IOException {
        CompilationUnit cu = StaticJavaParser.parse(source);

        // 2. 遍历所有方法，在方法体第一行插入代码
        cu.findAll(MethodDeclaration.class, methodDeclaration -> methodDeclaration.getAnnotationByClass(IDataAccessObject.MixColumn.class).isPresent())
                .forEach(method -> {
                    method.getBody().ifPresent(body -> {
                        for (Node childNode : new LinkedList<>(body.getChildNodes())) {
                            body.remove(childNode);
                        }
                        body.addStatement(" System.out.println(\"ASM 增强\");");
                        body.addStatement(" return null;");
                    });
                });

        // 3. 输出到 target/generated-sources/modified
        try (Writer writer = fileObject.openWriter()) {
            writer.write(cu.toString());
        }
    }*/


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(ExecuteWrapper.class);
            if (CollectionUtils.isEmpty(set)) return true;
            BaseProxyProcessor.setLogger((o, o2) -> {
                log(Diagnostic.Kind.NOTE, o, o2);
            });
            for (Element element : set) {
                String aClass = null;
                for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                    if (ExecuteWrapper.class.getName().equals(annotationMirror.getAnnotationType().toString())) {
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                            if (entry.getKey().getSimpleName().toString().equals("value")) {
                                aClass = entry.getValue().getValue().toString();
                                break;
                            }
                        }
                    }
                    if (aClass != null) {
                        break;
                    }
                }
                log(Diagnostic.Kind.NOTE, "!!!!!!!!!!!!!{0}", aClass);
                BaseProxyProcessor baseProxyProcessor = (BaseProxyProcessor) Class.forName(aClass).getConstructor(TypeElement.class).newInstance((TypeElement) element);

                baseProxyProcessor.toJava((name, raw) -> {
                    try {
                        log(Diagnostic.Kind.NOTE, "!!!!!!!!!!!!!{0}\n{1}", name, raw);
                        FileObject fileObject = processingEnv.getFiler().createSourceFile(name);
                        try (Writer writer = fileObject.openWriter()) {
                            writer.write(raw);
                        }
                        log(Diagnostic.Kind.NOTE, "!!!!!!!!!!!!!{0}", fileObject.toUri().getPath());
                    } catch (Throwable e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Throwables.getStackTraceAsString(e));
                    }
                });
//                fileObject.openWriter().write(raw);
            }
        } catch (Throwable e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, Throwables.getStackTraceAsString(e));
        }
        return true;
    }

   /* public final static List<String> excludeMethod = Arrays.asList("wait[\"long\"]", "wait[\"long\",\"int\"]", "wait[]", "equals[\"java.lang.Object\"]", "toString[]", "hashCode[]", "getClass[]", "notify[]", "notifyAll[]");

    public static String toJava(TypeElement target) {
        TypeElement superClass = target.getSuperclass().getKind() != TypeKind.NONE ? (TypeElement) ((DeclaredType) target.getSuperclass()).asElement() : null;
        superClass = superClass.getQualifiedName().toString().equals(Object.class.getName()) ? null : superClass;
        if (superClass != null) {
            toJava(superClass);
        }
        StringBuilder builder1 = new StringBuilder("    public Object _apply(String key, Object o, Object[] o1) { \n"), builder2 = new StringBuilder("    public Class<?>[] _apply(String key) { \n");
        append("        ", builder1, builder2);

        String packageName = target.getQualifiedName().toString().substring(0, target.getQualifiedName().toString().lastIndexOf("."));
        append("switch(key) {\n", builder1, builder2);
        for (Element element : target.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) continue;
            ExecutableElement method = (ExecutableElement) element;
            if (!method.getModifiers().contains(Modifier.PUBLIC)) continue;
            String methodName = method.getSimpleName().toString();
            List<String> parameterTypes = method.getParameters().stream().map(ExecuteWrapperProcessor::getTypeFullName).collect(Collectors.toList());
            String returnTypes = getReturnFullType(method);
            String key = toKey(method.getSimpleName().toString(), parameterTypes.toString());
            if (excludeMethod.contains(key)) continue;
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
            builder1.append("((").append(target.getQualifiedName().toString()).append(")o).").append(methodName).append("(");
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
        if (superClass != null) {
            append("return super._apply(key", builder1, builder2);
            builder1.append(",o,o1");
            append(");\n", builder1, builder2);
        } else {
            append("throw new IllegalStateException(\"不支持的方法:" + getExecutorName(target) + "\"+key);\n", builder1, builder2);
        }

        append("            ", builder1, builder2);
        append("}\n", builder1, builder2);
        append("      ", builder1, builder2);
        append("}\n", builder1, builder2);

        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n");
        builder.append("public class ").append(getExecutorName(target, false));
        if (superClass != null) {
            builder.append(" extends ").append(getExecutorName(superClass));
        } else {
            builder.append(" implements org.welisdoon.common.object.wrapper.execute.Executor");
        }
        builder.append(" { \n").append(builder1).append("\n").append(builder2).append("}\n");
        return builder.toString();
    }

    public static void append(String path, StringBuilder... builder) {
        for (StringBuilder stringBuilder : builder) {
            stringBuilder.append(path);
        }
    }

    public static String getExecutorName(TypeElement target) {
        return getExecutorName(target, true);
    }

    public static String getExecutorName(TypeElement target, boolean showPackage) {
        String name = target.getQualifiedName().toString();
        if (!showPackage) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }
        return name + "ProxyExecutor";
    }

    public static String toKey(String methodName, String parameterTypes) {
        return methodName + parameterTypes;
    }

    public void log(Diagnostic.Kind kind, String template, Object... objects) {
        processingEnv.getMessager().printMessage(kind, MessageFormat.format("{0} {1}", this.getClass().getName(), MessageFormat.format(template, objects)));
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
    }*/
}
