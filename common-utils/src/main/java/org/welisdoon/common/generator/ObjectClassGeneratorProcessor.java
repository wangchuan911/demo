package org.welisdoon.common.generator;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("org.welisdoon.common.generator.Agent")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ObjectClassGeneratorProcessor extends AbstractProcessor {

    private Filer mFiler;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<Class<?>, Object> elementObjectMap = new HashMap<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Agent.class)) {
            analysisAnnotated(annotatedElement, elementObjectMap);
        }
        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
//        mFiler = processingEnv.getFiler();
    }

    private void analysisAnnotated(Element classElement, Map<Class<?>, Object> classObjectMap) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "xxxxxxxxxxxxxx" + classElement.getSimpleName());
//        switch (classElement.getKind()) {
//            case INTERFACE:
//            case CLASS:
//                try {
//                    Class<?> aClass = Class.forName(((TypeElement) classElement).getQualifiedName().toString());
//                    Class<?> intf = null;
//                    for (Class<?> anInterface : aClass.getInterfaces()) {
//                        if (!IAgent.class.isAssignableFrom(anInterface)) {
//                            continue;
//                        }
//                        if (intf == null || intf.isAssignableFrom(anInterface)) {
//                            intf = anInterface;
//                        }
//                    }
//                    if (intf == null)
//                        return;
//
//                    classObjectMap.put(aClass, null);
//                    if (classElement.getKind() == ElementKind.CLASS) {
//                        Arrays.stream(aClass.getInterfaces()).filter(IAgent.class::isAssignableFrom)
//                                .findFirst().ifPresent(classObjectMap::remove);
//                    }
//                } catch (ClassNotFoundException e) {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
//                }
//
//
//                System.out.println(classElement.getSimpleName());
//
//
//        }
        /*int id = annotation.id();
        String name = annotation.name();
        String text = annotation.text();
        String newClassName = name + "Agent";

        StringBuilder builder = new StringBuilder()
                .append("package com.autotestdemo.maomao.autotestdemo.auto;\n\n")
                .append("public class ")
                .append(newClassName)
                .append(" {\n\n") // open class
                .append("\tpublic String getMessage() {\n") // open method
                .append("\t\treturn \"");

        // this is appending to the return statement
        builder.append(id).append(text).append(newClassName).append(" !\\n");


        builder.append("\";\n") // end returne
                .append("\t}\n") // close method
                .append("}\n"); // close class


        try { // write the file
            JavaFileObject source = mFiler.createSourceFile("com.autotestdemo.maomao.autotestdemo.auto." + newClassName);
            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }*/
    }
}
