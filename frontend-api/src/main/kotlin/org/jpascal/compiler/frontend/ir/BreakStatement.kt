package org.jpascal.compiler.frontend.ir

class BreakStatement(
    override val jump: Label?,
    override var label: Label? = null,
    override val position: SourcePosition?,
    override var parent: PositionedElement? = null
) : JumpStatement