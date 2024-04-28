package org.jpascal.compiler.frontend.ir

class AssignmentStatement(
    val variable: Variable,
    val expression: Expression,
    override var label: Label? = null,
    override val position: SourcePosition? = null
) : Statement {
    init {
        variable.parent = this
        expression.parent = this
    }

    override var parent: PositionedElement? = null
}