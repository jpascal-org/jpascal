package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.BooleanType
import org.jpascal.compiler.frontend.ir.types.Type

class BooleanLiteral(val value: Boolean, override val position: SourcePosition?) : Expression {
    override val type: Type = BooleanType
    override var parent: PositionedElement? = null
}