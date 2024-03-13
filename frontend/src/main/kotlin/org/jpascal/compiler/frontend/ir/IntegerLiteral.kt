package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.ir.types.Type

data class IntegerLiteral(val value: Int) : Expression {
    override val type: Type
        get() = IntegerType
}