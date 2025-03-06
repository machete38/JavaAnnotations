package com.machete3845.java_annotations;


import com.google.android.gms.vision.text.Element;
import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Flow;

@AutoService(Flow.Processor.class)
public class LogMethodProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        return ImmutableSet.of(new LogMethodProcessingStep());
    }

    private class LogMethodProcessingStep implements ProcessingStep{


        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return ImmutableSet.of(LogMethod.class);
        }

        @Override
        public Set<? extends javax.lang.model.element.Element> process(SetMultimap<Class<? extends Annotation>, javax.lang.model.element.Element> elementsByAnnotation) {
            Set<Element> elementsProcessed = new HashSet<>();

            for (Element element : elementsByAnnotation.get(LogMethod.class)) {
                if (element.getKind() != ElementKind.METHOD) {
                    continue;
                }

                LogMethod annotation = element.getAnnotation(LogMethod.class);
                String methodName = element.getSimpleName().toString();
                String className = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
                String packageName = className.substring(0, className.lastIndexOf('.'));
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                String loggerClassName = simpleClassName + "_" + methodName + "_Logger";

                MethodSpec logMethod = MethodSpec.methodBuilder("logMethod")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(Object[].class, "args")
                        .addStatement("android.util.Log.$L($S, $S + java.util.Arrays.toString(args))",
                                annotation.level().toString().toLowerCase(),
                                simpleClassName,
                                methodName + " called with args: ")
                        .build();

                MethodSpec logResult = MethodSpec.methodBuilder("logResult")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(void.class)
                        .addParameter(Object.class, "result")
                        .addStatement("android.util.Log.$L($S, $S + result)",
                                annotation.level().toString().toLowerCase(),
                                simpleClassName,
                                methodName + " returned: ")
                        .build();

                TypeSpec loggerClass = TypeSpec.classBuilder(loggerClassName)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(logMethod)
                        .addMethod(logResult)
                        .build();

                JavaFile javaFile = JavaFile.builder(packageName, loggerClass)
                        .build();

                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Failed to write logger class: " + e.getMessage(), element);
                }

                elementsProcessed.add(element);
            }

            return elementsProcessed;
        }
    }
}
