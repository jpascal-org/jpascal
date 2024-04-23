package org.jpascal.compiler.frontend.ir

data class WhileStatement(
    val condition: Expression,
    val statement: Statement,
    override var label: Label? = null,
    override val position: SourcePosition?
) : Statement