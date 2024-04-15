package org.jpascal.compiler.frontend.ir

data class WhileStatement(
    val condition: Expression,
    val statement: Statement,
    override val label: Label? = null
) : Statement