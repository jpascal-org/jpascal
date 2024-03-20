package org.jpascal.compiler.frontend.ir

data class CompoundStatement(
    val statements: List<Statement>,
    override val label: Label? = null
): Statement