package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.IntegerType

data class IntegerNumber(val value: Int, override val position: SourcePosition? = null) : Expression {
    override var type = IntegerType
}