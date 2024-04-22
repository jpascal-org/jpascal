package org.jpascal.compiler.frontend.ir

data class ReturnStatement(
    val expression: Expression?,
    override val label: Label? = null,
    override val position: SourcePosition? = null
) : Statement
