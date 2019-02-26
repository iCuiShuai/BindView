package com.csdroid.demo.apt.processor;

import com.csdroid.demo.apt.annotation.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**@AutoServic用于自动生成META-INF信息*/
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BindProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Map<TypeElement, Set<Element>> elements;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new HashSet<>();
        result.add(BindView.class.getCanonicalName());
        return result;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elements = new HashMap<>();
        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        elements.clear();

        System.out.println(BindProcessor.class.getSimpleName() + " process started!");

        parseBindElements(roundEnvironment.getElementsAnnotatedWith(BindView.class));

        System.out.println(BindProcessor.class.getSimpleName() + " process end!");
        return doProcess();
    }

    private boolean doProcess() {
        if (elements.isEmpty()) {
            return false;
        }
        for (TypeElement enclosingElement : elements.keySet()) {
            MethodSpec methodSpec = generateMethodSpec(enclosingElement);

            TypeSpec typeSpec = generateTypeSpec(enclosingElement, methodSpec);

            JavaFile javaFile = generateJavaFile(enclosingElement, typeSpec);

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private JavaFile generateJavaFile(TypeElement enclosingElement, TypeSpec typeSpec) {
        return JavaFile.builder(elementUtils.getPackageOf(enclosingElement).getQualifiedName().toString(), typeSpec).build();
    }

    private TypeSpec generateTypeSpec(TypeElement enclosingElement, MethodSpec methodSpec) {
        return TypeSpec.classBuilder(enclosingElement.getSimpleName() + "Bind")
                        .superclass(TypeName.get(enclosingElement.asType()))
                        .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                        .addMethod(methodSpec)
                        .build();
    }

    private MethodSpec generateMethodSpec(TypeElement enclosingElement) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get(enclosingElement.asType()), "activity")
                .returns(TypeName.VOID);
        Set<Element> bindSet = elements.get(enclosingElement);
        for (Element element : bindSet) {
            builder.addStatement(String.format(Locale.US,"activity.%s = (%s)activity.findViewById(%d)", element.getSimpleName(), element.asType(), element.getAnnotation(BindView.class).value()));
        }
        return builder.build();
    }

    private void parseBindElements(Set<? extends Element> bindElements) {
        if (bindElements == null || bindElements.isEmpty()) {
            return;
        }

        for (Element element : bindElements) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            Set<Element> set = elements.get(enclosingElement);
            if (set == null) {
                set = new HashSet<>();
                elements.put(enclosingElement, set);
            }
            set.add(element);
            System.out.println("Add element :element name:" + element.getSimpleName() + "\tenclosingElement name:"+enclosingElement.getSimpleName());
        }


    }
}
