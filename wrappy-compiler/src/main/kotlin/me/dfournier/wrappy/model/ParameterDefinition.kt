package me.dfournier.wrappy.model

import javax.lang.model.type.TypeMirror

data class ParameterDefinition(
        val name: String,
        val type: TypeMirror
)