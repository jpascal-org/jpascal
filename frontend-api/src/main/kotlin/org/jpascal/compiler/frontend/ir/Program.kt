package org.jpascal.compiler.frontend.ir

class Program(
    val packageName: String?,
    val uses: List<Uses>,
    val declarations: Declarations,
    val compoundStatement: CompoundStatement?,
    override val position: SourcePosition?
) : PositionedElement {
    init {
        declarations.functions.forEach { it.parent = this }
        declarations.variables.forEach { it.parent = this }
        compoundStatement?.parent = this
    }

    override var parent: PositionedElement? = null
}
