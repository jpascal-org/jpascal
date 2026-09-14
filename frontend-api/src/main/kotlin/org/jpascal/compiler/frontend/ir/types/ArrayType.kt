package org.jpascal.compiler.frontend.ir.types

data class ArrayType(val indexTypes: List<OrderedType>, val elementType: Type) : Type