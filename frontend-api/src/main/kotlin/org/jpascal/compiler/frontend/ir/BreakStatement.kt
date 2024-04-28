package org.jpascal.compiler.frontend.ir

class BreakStatement(
    val jumpTo: Label?,
    override var label: Label? = null,
    override val position: SourcePosition?,
    override var parent: PositionedElement? = null
) : Statement