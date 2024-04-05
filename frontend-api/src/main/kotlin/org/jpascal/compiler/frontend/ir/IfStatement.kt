package org.jpascal.compiler.frontend.ir

data class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?,
    override val label: Label? = null,
    override val position: SourcePosition? = null
) : Statement, PositionedElement