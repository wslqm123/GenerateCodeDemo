package com.meitu.annotationprocessor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class MyAnnotationProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(PrintName.class.getCanonicalName());
        annotations.add(BindView.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        processPrintName(roundEnvironment);
        processBindView(roundEnvironment);
        return true;
    }

    private void processPrintName(RoundEnvironment roundEnvironment) {
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(PrintName.class);
        for (Element element : bindViewElements) {
            //1.获取包名
            PackageElement packageElement = mElementUtils.getPackageOf(element);
            String pkName = packageElement.getQualifiedName().toString();
            note(String.format("package = %s", pkName));

            //2.获取包装类类型
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String enclosingName = enclosingElement.getQualifiedName().toString();
            note(String.format("enclosindClass = %s", enclosingElement));


            //因为BindView只作用于filed，所以这里可直接进行强转
            VariableElement bindViewElement = (VariableElement) element;
            //3.获取注解的成员变量名
            String bindViewFiledName = bindViewElement.getSimpleName().toString();
            //3.获取注解的成员变量类型
            String bindViewFiledClassType = bindViewElement.asType().toString();

            //4.获取注解元数据
            PrintName printName = element.getAnnotation(PrintName.class);
            String data = printName.value();
            note(String.format("%s.%s = %s", bindViewFiledClassType, bindViewFiledName, data));

            //4.生成文件
            createFile(enclosingElement, bindViewFiledClassType, bindViewFiledName, data);
        }
    }

    private void processBindView(RoundEnvironment roundEnvironment) {
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : bindViewElements) {

            //因为BindView只作用于filed，所以这里可直接进行强转
            VariableElement bindViewElement = (VariableElement) element;
            String viewName = bindViewElement.getSimpleName().toString();

            BindView annotation = element.getAnnotation(BindView.class);
            int annotationId = annotation.value();
            ClassName view = ClassName.get("android.view", "View");
            ClassName textView = ClassName.get("android.widget", "TextView");
            ClassName activity = ClassName.get("android.app", "Activity");

            FieldSpec fieldId = FieldSpec.builder(int.class, "tvId")
                    .addModifiers(Modifier.PRIVATE)
                    .initializer("$L", annotationId)
                    .build();

            FieldSpec fieldView = FieldSpec.builder(textView, viewName)
                    .build();

            MethodSpec bindView = MethodSpec.methodBuilder("bindView")
                    .addParameter(activity, "activity")
                    .addStatement("$T decorView = $N.getWindow().getDecorView()", view, "activity")
                    .addStatement("$N = ($T) $N.findViewById($L)", viewName, textView, "decorView", annotationId)
                    .addStatement("$N.setText($S)", viewName, "bind view by generate code")
                    .build();

            TypeSpec typeSpec = TypeSpec.classBuilder("BufferKnife")
                    .addField(fieldId)
                    .addField(fieldView)
                    .addMethod(bindView)
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.meitu.generatecodedemo", typeSpec)
                    .build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFile(TypeElement enclosingElement, String filedClassType, String filedName, String value) {
        String pkName = mElementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
        try {
            JavaFileObject jfo = mFiler.createSourceFile(pkName + ".ClassPath", new Element[]{});
            Writer writer = jfo.openWriter();
            writer.write(brewCode(pkName, filedClassType, filedName, value));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String brewCode(String pkName, String filedClassType, String filedName, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("package " + pkName + ";\n\n");
        builder.append("//Auto generated by apt,do not modify!!\n\n");
        builder.append("public class ClassPath { \n\n");
        builder.append("public static void main(String[] args){ \n");
        String info = String.format("%s.%s = %s", filedClassType, filedName, value);
        builder.append("System.out.println(\"" + info + "\");\n");
        builder.append("}\n");
        builder.append("}");
        return builder.toString();
    }


    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void note(String format, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
    }

}
