package me.dfournier.wrappy

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Action
import me.dfournier.wrappy.model.AnnotationProcessingContext
import me.dfournier.wrappy.model.WrapperDefinition
import java.io.IOException
import java.util.concurrent.Callable
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeKind


@AutoService(WrappyGenerator::class)
class RxJava2Generator() : WrappyGenerator {

    override fun getName(): String = NAME

    override fun generate(context: AnnotationProcessingContext, wrapper: WrapperDefinition) {
        val filer = context.processingEnv.filer

        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(wrapper.wrappedClass), WRAPPED_CLASS)
                .addStatement("this.$WRAPPED_CLASS = $WRAPPED_CLASS")
                .build()

        val methods = wrapper.methods.map { methodDef ->
            val builder = MethodSpec.methodBuilder(methodDef.name)
                    .addModifiers(Modifier.PUBLIC)

            methodDef.params.forEach { paramDef ->
                builder.addParameter(TypeName.get(paramDef.type), paramDef.name, Modifier.FINAL)
            }

            val paramsStr = methodDef.params.map { it.name }.joinToString(", ")
            if (methodDef.returnType.kind != TypeKind.NONE
                    && methodDef.returnType.kind != TypeKind.VOID) {
                val isPrimitive = methodDef.returnType.kind.isPrimitive
                val returnTypeName =
                        if (isPrimitive) {
                            when (methodDef.returnType.kind) {
                                TypeKind.BOOLEAN -> TypeName.get(java.lang.Integer::class.java)
                                TypeKind.BYTE -> TypeName.get(java.lang.Byte::class.java)
                                TypeKind.SHORT -> TypeName.get(java.lang.Short::class.java)
                                TypeKind.INT -> TypeName.get(java.lang.Integer::class.java)
                                TypeKind.LONG -> TypeName.get(java.lang.Long::class.java)
                                TypeKind.CHAR -> TypeName.get(java.lang.Character::class.java)
                                TypeKind.FLOAT -> TypeName.get(java.lang.Float::class.java)
                                TypeKind.DOUBLE -> TypeName.get(java.lang.Double::class.java)
                                else -> throw IllegalStateException("Should not happen")    // TODO improve
                            }
                        } else {
                            TypeName.get(methodDef.returnType)
                        }

                // TODO manage Null safety
                val callable = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(
                                ParameterizedTypeName.get(
                                        ClassName.get(Callable::class.java),
                                        returnTypeName
                                )
                        )
                        .addMethod(
                                MethodSpec.methodBuilder("call")
                                        .addAnnotation(Override::class.java)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(returnTypeName)
                                        .apply {
                                            if (isPrimitive) {
                                                addStatement("return wrappedClass.${methodDef.name}($paramsStr)")
                                            } else {
                                                addStatement("\$T value = wrappedClass.${methodDef.name}($paramsStr)", returnTypeName)
                                                beginControlFlow("if (value != null)")
                                                addStatement("return value")
                                                endControlFlow()
                                                addStatement("throw new \$T()", java.lang.IllegalStateException::class.java)
                                            }
                                        }
                                        .build()
                        )
                        .build()

                builder
                        .returns(
                                ParameterizedTypeName.get(
                                        ClassName.get(Single::class.java),
                                        returnTypeName
                                )
                        )
                        .addStatement("return \$T.fromCallable(\$L)", Single::class.java, callable)
            } else {

                val action = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(TypeName.get(Action::class.java))
                        .addMethod(
                                MethodSpec.methodBuilder("run")
                                        .addAnnotation(Override::class.java)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addStatement("wrappedClass.${methodDef.name}($paramsStr)")
                                        .build()
                        )
                        .build()

                builder
                        .returns(TypeName.get(Completable::class.java))
                        .addStatement("return \$T.fromAction(\$L)", Completable::class.java, action)
            }

            builder.build()
        }


        val typeSpec = TypeSpec.classBuilder(wrapper.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(TypeName.get(wrapper.wrappedClass), WRAPPED_CLASS, Modifier.PRIVATE)
                .addMethod(constructor)
                .addMethods(methods)

        val file = JavaFile.builder(wrapper.packageName, typeSpec.build())
                .addFileComment("Generated File, do not modify")
                .build()

        try {
            file.writeTo(filer)
        } catch (e: IOException) {
        }

    }


    companion object {
        val NAME = "RxJava2"
        private val WRAPPED_CLASS = "wrappedClass"
    }

}