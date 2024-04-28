package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type
import org.jpascal.compiler.frontend.resolve.JvmField

class Variable(
    val name: String,
    override val position: SourcePosition? = null,
    override var type: Type? = null,
    var jvmField: JvmField? = null
) : Expression {
    override var parent: PositionedElement? = null
}