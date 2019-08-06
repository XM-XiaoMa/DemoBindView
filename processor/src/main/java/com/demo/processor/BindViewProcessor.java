package com.demo.processor;

import com.demo.annotation.BindView;
import com.demo.annotation.DIActivity;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.demo.annotation.DIActivity"})
public class BindViewProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null) return false;
        // 获取被DiActivity注解的节点
        TypeMirror activityTypeMirror = elementUtils.getTypeElement("android.app.Activity").asType();
        Set<? extends Element> dIActivityNodes = roundEnvironment.getElementsAnnotatedWith(DIActivity.class);
        for (Element dIActivityNode : dIActivityNodes) {
            TypeMirror typeMirror = dIActivityNode.asType();
            DIActivity annotation = dIActivityNode.getAnnotation(DIActivity.class);
            if (!typeUtils.isSubtype(typeMirror, activityTypeMirror))
                throw new IllegalArgumentException("@DIActivity must of Activity");
            // 创建参数
            ParameterSpec bindParam = ParameterSpec.builder(ClassName.get(typeMirror), "activity")
                    .build();
            // 创建方法
            MethodSpec.Builder bindMethodBuild = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(TypeName.VOID)
                    .addParameter(bindParam);
            // 创建方法体
            TypeElement classElement = (TypeElement) dIActivityNode;
            List<? extends Element> members = elementUtils.getAllMembers(classElement);
            for (Element member : members) {
                // 找到BindView注解的成员变量
                BindView bindViewAnno = member.getAnnotation(BindView.class);
                if (bindViewAnno == null) continue;
                // 构建方法体
                bindMethodBuild.addStatement(String.format("activity.%s = (%s) activity.findViewById(%s)",
                        member.getSimpleName(), ClassName.get(member.asType()).toString(), bindViewAnno.value()));

            }
            // 创建类
            TypeSpec typeSpec = TypeSpec.classBuilder("BindView_" + dIActivityNode.getSimpleName())
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(bindMethodBuild.build())
                    .build();
            // 创建java class文件
            JavaFile javaClass = JavaFile.builder("com.demo.bind", typeSpec).build();
            try {
                javaClass.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
