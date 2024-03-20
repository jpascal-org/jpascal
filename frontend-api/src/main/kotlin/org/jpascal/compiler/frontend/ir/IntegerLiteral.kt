package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.IntegerType

data class IntegerLiteral(val value: Int) : Expression {
    override var type = IntegerType
}