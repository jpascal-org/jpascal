package org.jpascal.compiler.frontend.ir

class ForStatement(
    val variable: Variable,
    val initialValue: Expression,
    val finalValue: Expression,
    val isDecrement: Boolean,
    val statement: Statement,
    override var label: Label? = null,
    override val position: SourcePosition?
) : Statement {
    init {
        variable.parent = this
        statement.parent = this
        initialValue.parent = this
        finalValue.parent = this
        statement.parent = this
    }

    override var parent: PositionedElement? = null
}