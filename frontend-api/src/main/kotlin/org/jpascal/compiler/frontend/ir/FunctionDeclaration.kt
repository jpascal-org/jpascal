package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type
import org.jpascal.compiler.frontend.ir.types.UnitType

data class FunctionDeclaration(
    val identifier: String,
    val params: List<FormalParameter>,
    val returnType: Type,
    val access: Access,
    val declarations: Declarations?,
    val compoundStatement: CompoundStatement,
    override val position: SourcePosition
) : PositionedElement {
    fun isProcedure() = returnType == UnitType
}