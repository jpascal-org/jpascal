package org.jpascal.compiler.frontend.ir

class ReturnStatement(
    val expression: Expression?,
    override var label: Label? = null,
    override val position: SourcePosition? = null
) : Statement {
    init {
        expression?.parent = this
    }

    override var parent: PositionedElement? = null
}
