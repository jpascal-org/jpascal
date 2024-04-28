package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.IntegerType
class IntegerNumber(val value: Int, override val position: SourcePosition? = null) : Expression {
    override var type = IntegerType
    override var parent: PositionedElement? = null
}