package me.dfournier.wrappy.model

import javax.lang.model.type.TypeMirror

data class MethodDefinition(
        val name: String,
        val returnType: TypeMirror,
        val params: List<ParameterDefinition>
)