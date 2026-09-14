package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.BooleanType
import org.jpascal.compiler.frontend.ir.types.Type

class BooleanLiteral(override val value: Boolean, override val position: SourcePosition?) : Expression,
    OrderedValue<Boolean> {
    override val type: Type = BooleanType
    override var parent: PositionedElement? = null
}