package org.jpascal.compiler.frontend.ir

data class RepeatStatement(
    val condition: Expression,
    val statement: Statement,
    override val label: Label? = null,
    override val position: SourcePosition?
) : Statement