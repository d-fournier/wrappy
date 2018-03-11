package me.dfournier.wrappy

import me.dfournier.wrappy.model.AnnotationProcessingContext
import me.dfournier.wrappy.model.WrapperDefinition

interface WrappyGenerator {


    fun getName(): String

    fun generate(context: AnnotationProcessingContext, wrapper: WrapperDefinition)

}