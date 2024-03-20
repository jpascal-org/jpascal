package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

data class VariableDeclaration(
    val name: String,
    val type: Type,
    val expr: Expression?
)