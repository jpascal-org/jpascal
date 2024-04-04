package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type
import org.jpascal.compiler.frontend.resolve.JvmField

data class Variable(
    val name: String,
    override val position: SourcePosition? = null,
    override var type: Type? = null,
    var jvmField: JvmField? = null
) : Expression