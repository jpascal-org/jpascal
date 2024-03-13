package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type
import org.jpascal.compiler.frontend.ir.types.UnitType

data class Function(
    val name: String,
    val params: List<Parameter>,
    val returnType: Type,
    val declarations: Declarations?,
    val block: Block,
    override val position: SourcePosition
) : PositionedElement {
    fun isProcedure() = returnType == UnitType
}