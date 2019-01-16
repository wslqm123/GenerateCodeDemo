package com.meitu.annotationprocessor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import javax.lang.model.element.Modifier;


/**
 * JavaPoetSample
 * extra: JavaPoetSample
 * Created by LQM<lqm1@meitu.com> on 2018/12/8 - 9:28 PM
 */
public class JavaPoetSample {


    public static void main(String[] args) {
        helloworld();
//        System.out.println(buildField());
//        System.out.println(buildAnnotation());
//        System.out.println(buildMethod(3));
//        System.out.println(buildType(3));
//        System.out.println(buildFile());
        System.out.println(placehodler(3));
    }

    private static void helloworld() {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        MethodSpec contruct = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "greeting")
                .addStatement("this.$N = $N", "greeting", "greeting")
                .build();


        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .addField(String.class, "greeting", Modifier.PRIVATE)
                .addMethod(contruct)
                .addJavadoc("This class is generated and should not be modified")
                .build();

        JavaFile javaFile = JavaFile.builder("com.meitu.generatecode", helloWorld)
                .build();


        try {
            javaFile.writeTo(getDirectory());
        } catch (Exception e) {

        }

    }

    /**
     * 构造变量
     *
     * @return
     */
    private static FieldSpec buildField() {
        return FieldSpec.builder(String.class, "mField", Modifier.PRIVATE)
                .initializer("$S", "123")
                .build();
    }

    /**
     * 构造注解
     *
     * @return
     */
    private static AnnotationSpec buildAnnotation() {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(PrintName.class)
                .addMember("value", "$S", "print class name");
        return builder.build();
    }

    /**
     * 构造方法
     *
     * @param type
     */
    private static MethodSpec buildMethod(int type) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("methodDemo");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        switch (type) {
            case 0:
                // 添加注解
                builder.addParameter(String[].class, "args")
                        .addAnnotation(Override.class)
                        .addAnnotation(buildAnnotation())
                        .addStatement("$T.out.println($S)", System.class, "method statement")
                        .returns(void.class);
                break;
            case 1:
                // 直接添加代码
                builder.addCode(""
                        + "int total = 0;\n"
                        + "for (int i = 0; i < 10; i++) {\n"
                        + "  total += i;\n"
                        + "}\n")
                        .returns(void.class);
                break;
            case 2:
                // 代码块
                builder.addStatement("int total = 0")
                        .beginControlFlow("for (int i = 0; i < 10; i++)")
                        .addStatement("total += i")
                        .endControlFlow()
                        .returns(void.class);
                break;
            case 3:
                // 返回值
                builder.addParameter(int.class, "a")
                        .addParameter(int.class, "b")
                        .addStatement("return $N - $N", "a", "b")
                        .returns(int.class);
                break;
            case 4:
                // 构造函数见 helloworld()
                break;

        }
        return builder.build();
    }


    /**
     * 构造类
     *
     * @return
     */
    private static TypeSpec buildType(int type) {

        TypeSpec.Builder builder = null;

        switch (type) {
            case 0:
                // 普通类
                builder = TypeSpec.classBuilder("ClassDemo");
                builder.addModifiers(Modifier.PUBLIC)
                        .addField(buildField())
                        .addMethod(buildMethod(0));
                break;
            case 1:
                // 接口
                builder = TypeSpec.interfaceBuilder("InterfaceDemo");
                builder.addModifiers(Modifier.PUBLIC)
                        .addMethod(MethodSpec.methodBuilder("invoke")
                                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                .build()
                        );
                break;
            case 2:
                // 枚举
                builder = TypeSpec.enumBuilder("EnumDemo");
                builder.addModifiers(Modifier.PUBLIC)
                        .addEnumConstant("SUCCESS")
                        .addEnumConstant("FAIL")
                        .addEnumConstant("DEFAULT", TypeSpec.anonymousClassBuilder("$S", "other")
                                .build());
                break;
            case 3:
                // 内部类
                TypeSpec innerType = TypeSpec.classBuilder("InnerType")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(buildMethod(3))
                        .build();
                // 匿名内部类
                TypeSpec anonymousType = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(OnFunInvoke.class)
                        .addMethod(MethodSpec.methodBuilder("invoke")
                                .addAnnotation(Override.class)
                                .addStatement("$T.out.println($S)", System.class, "method override")
                                .build())
                        .build();


                builder = TypeSpec.classBuilder("ClassDemo");
                builder.addModifiers(Modifier.PUBLIC)
                        .addField(HashMap.class, "mData", Modifier.PRIVATE)
                        .addMethod(MethodSpec.methodBuilder("addData")
                                .addModifiers(Modifier.PUBLIC)
                                .addStatement("$N.add($S, $L)", "mData", "key", anonymousType)
                                .build())
                        .addType(innerType);
                break;

        }

        return builder.build();
    }


    /**
     * 构造javafile
     *
     * @return
     */
    private static JavaFile buildFile() {
        // 支持import static
        return JavaFile.builder("com.meitu.generate", buildType(0))
                .addStaticImport(Integer.class, "size")
                .build();
    }


    /**
     * 占位符
     *
     * @return
     */
    private static MethodSpec placehodler(int type) {
        switch (type) {
            case 0:
                // $L
                int from = 0;
                int to = 10;
                String op = "+";
                return MethodSpec.methodBuilder("literalsHolder")
                        .returns(int.class)
                        .addStatement("int result = 0")
                        .beginControlFlow("for (int i = $L; i < $L; i++)", from, to)
                        .addStatement("result = result $L i", op)
                        .endControlFlow()
                        .addStatement("return result")
                        .build();
            case 1:
                // $S
                String name = "zhang san";
                return MethodSpec.methodBuilder("stringHolder")
                        .returns(String.class)
                        .addStatement("return $S", name)
                        .build();

            case 2:
                // $T
                return MethodSpec.methodBuilder("typeHolder")
                        .returns(Date.class)
                        .addStatement("return new $T()", Date.class)
                        .build();

            case 3:
                // $N
                MethodSpec hexDigit = MethodSpec.methodBuilder("hexDigit")
                        .addParameter(int.class, "i")
                        .returns(char.class)
                        .addStatement("return (char) (i < 10 ? i + '0' : i - 10 + 'a')")
                        .build();

                return MethodSpec.methodBuilder("byteToHex")
                        .addParameter(int.class, "b")
                        .returns(String.class)
                        .addStatement("char[] result = new char[2]")
                        .addStatement("result[0] = $N((b >>> 4) & 0xf)", hexDigit)
                        .addStatement("result[1] = $N(b & 0xf)", hexDigit)
                        .addStatement("return new String(result)")
                        .build();
        }

        return null;
    }


    /**
     * 指定生成目录
     *
     * @return
     */
    private static File getDirectory() {
        File dir = new File("annotationprocessor/src/main/java");
        return dir;
    }

    public interface OnFunInvoke {
        void invoke();
    }
}
