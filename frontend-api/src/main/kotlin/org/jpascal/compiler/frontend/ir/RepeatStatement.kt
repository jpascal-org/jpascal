package org.jpascal.compiler.frontend.ir

class RepeatStatement(
    val condition: Expression,
    val statement: Statement,
    override var label: Label? = null,
    override val position: SourcePosition?
) : Statement {
    init {
        condition.parent = this
        statement.parent = this
    }

    override var parent: PositionedElement? = null
}