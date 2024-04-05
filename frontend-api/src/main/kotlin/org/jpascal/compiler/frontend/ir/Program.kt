package org.jpascal.compiler.frontend.ir

data class Program(
    val packageName: String?,
    val uses: List<Uses>,
    val declarations: Declarations,
    val compoundStatement: CompoundStatement?,
    override val position: SourcePosition?
) : PositionedElement
