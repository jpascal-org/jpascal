package org.jpascal.compiler.frontend.ir

data class Declarations(
    val functions: List<Function>,
    val variables: List<VariableDeclaration>
)
