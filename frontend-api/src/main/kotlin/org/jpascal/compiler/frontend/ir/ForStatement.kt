package org.jpascal.compiler.frontend.ir

data class ForStatement(
    val variable: Variable,
    val initialValue: Expression,
    val finalValue: Expression,
    val isDecrement: Boolean,
    val statement: Statement,
    override var label: Label? = null,
    override val position: SourcePosition?
) : Statement