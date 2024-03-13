package org.jpascal.compiler.frontend.ir

data class Program(
    val name: String?,
    val uses: Uses?,
    val declarations: Declarations?,
    val block: Block,
    override val position: SourcePosition
) : PositionedElement
