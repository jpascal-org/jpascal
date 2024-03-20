package org.jpascal.compiler.frontend.ir

data class Declarations(
    val functions: List<FunctionDeclaration>,
    val variables: List<VariableDeclaration>
)
