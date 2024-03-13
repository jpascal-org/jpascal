package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.ir.types.Type

data class ArithmeticExpression(
    val op: ArithmeticOperation,
    val left: Expression,
    val right: Expression
) : Expression {
    override val type: Type
        get() = IntegerType // FIXME
}