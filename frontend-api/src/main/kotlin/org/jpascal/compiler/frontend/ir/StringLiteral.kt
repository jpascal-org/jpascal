package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.StringType
import org.jpascal.compiler.frontend.ir.types.Type

data class StringLiteral(val value: String) : Expression {
    override val type: Type = StringType
}