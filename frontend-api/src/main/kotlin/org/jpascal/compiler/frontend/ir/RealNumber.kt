package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.RealType

class RealNumber(val value: Double, override val position: SourcePosition? = null) : Expression {
    override var type = RealType
    override var parent: PositionedElement? = null
}