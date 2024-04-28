package org.jpascal.compiler.frontend.ir

class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?,
    override var label: Label? = null,
    override val position: SourcePosition? = null
) : Statement {
    init {
        condition.parent = this
        thenBranch.parent = this
        elseBranch?.parent = this
    }

    override var parent: PositionedElement? = null
}