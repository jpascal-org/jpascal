package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.IntegerType

data class IntegerNumber(val value: Int) : Expression {
    override var type = IntegerType
}