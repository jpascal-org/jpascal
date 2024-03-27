package org.jpascal.compiler.frontend.ir

data class Declarations(
    val functions: List<FunctionDeclaration> = listOf(),
    val variables: List<VariableDeclaration> = listOf()
)
