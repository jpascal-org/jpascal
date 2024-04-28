package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.StringType
import org.jpascal.compiler.frontend.ir.types.Type

class StringLiteral(val value: String, override val position: SourcePosition?) : Expression {
    override val type: Type = StringType
    override var parent: PositionedElement? = null
}