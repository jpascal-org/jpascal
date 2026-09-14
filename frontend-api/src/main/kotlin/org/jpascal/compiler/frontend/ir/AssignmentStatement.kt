package org.jpascal.compiler.frontend.ir

class AssignmentStatement(
    val LValue: Lvalue,
    val expression: Expression,
    override var label: Label? = null,
    override val position: SourcePosition? = null
) : Statement {
    init {
        LValue.parent = this
        expression.parent = this
    }

    override var parent: PositionedElement? = null
}