package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.RealType

data class RealNumber(val value: Double, override val position: SourcePosition? = null) : Expression {
    override var type = RealType
}