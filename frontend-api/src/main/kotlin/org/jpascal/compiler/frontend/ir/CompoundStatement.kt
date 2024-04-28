package org.jpascal.compiler.frontend.ir

class CompoundStatement(
    val statements: List<Statement>,
    override var label: Label? = null,
    override val position: SourcePosition? = null
): Statement {
    init {
        statements.forEach { it.parent = this }
    }

    override var parent: PositionedElement? = null
}