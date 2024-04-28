package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type
import org.jpascal.compiler.frontend.ir.types.UnitType
import org.jpascal.compiler.frontend.resolve.JvmMethod

class FunctionDeclaration(
    val identifier: String,
    val params: List<FormalParameter>,
    val returnType: Type,
    val access: Access,
    val declarations: Declarations,
    val compoundStatement: CompoundStatement,
    override val position: SourcePosition?,
    var jvmMethod: JvmMethod? = null
) : PositionedElement {
    init {
        params.forEach { it.parent = this }
        declarations.variables.forEach { it.parent = this }
        declarations.functions.forEach { it.parent = this }
        compoundStatement.parent = this
    }

    override var parent: PositionedElement? = null
    fun isProcedure() = returnType == UnitType
}