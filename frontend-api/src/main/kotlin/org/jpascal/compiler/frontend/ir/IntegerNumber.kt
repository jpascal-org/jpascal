package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.IntegerType

class IntegerNumber(override val value: Int, override val position: SourcePosition? = null) : Expression,
    OrderedValue<Int> {
    override var type = IntegerType
    override var parent: PositionedElement? = null
}