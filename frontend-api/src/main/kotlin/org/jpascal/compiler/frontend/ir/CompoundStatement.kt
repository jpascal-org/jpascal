package org.jpascal.compiler.frontend.ir

data class CompoundStatement(
    val statements: List<Statement>,
    override val label: Label? = null,
    override val position: SourcePosition? = null
): Statement, PositionedElement