package me.dfournier.wrappy.generator

import com.google.auto.service.AutoService
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import me.dfournier.wrappy.WrappyGenerator
import me.dfournier.wrappy.model.AnnotationProcessingContext
import me.dfournier.wrappy.model.WrapperDefinition
import java.io.IOException
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeKind

@AutoService(WrappyGenerator::class)
class EmptyClassGenerator : WrappyGenerator {

    override fun getName(): String = NAME

    override fun generate(context: AnnotationProcessingContext, wrapper: WrapperDefinition) {
        val filer = context.processingEnv.filer

        val varName = "wrappedClass"
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(wrapper.wrappedClass), varName)
                .addStatement("this.$varName = $varName")
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
                builder.returns(TypeName.get(methodDef.returnType))
                builder.addStatement("return wrappedClass.${methodDef.name}($paramsStr)")
            } else {
                builder.addStatement("wrappedClass.${methodDef.name}($paramsStr)")
            }

            builder.build()
        }

        val typeSpec = TypeSpec.classBuilder(wrapper.className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(TypeName.get(wrapper.wrappedClass), varName, Modifier.PRIVATE)
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
        val NAME = "Empty"
    }
}