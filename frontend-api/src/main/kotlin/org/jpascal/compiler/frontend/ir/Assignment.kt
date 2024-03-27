package org.jpascal.compiler.frontend.ir

data class Assignment(
    val variable: Variable,
    val expression: Expression,
    override val label: Label? = null,
    override val position: SourcePosition? = null
) : Statement, PositionedElement