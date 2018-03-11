package me.dfournier.wrappy.model

import javax.lang.model.element.Element
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

data class WrapperDefinition(
        val packageName: String,
        val className: String,
        val wrappedClass: TypeMirror,
        val methods: List<MethodDefinition>
)