package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.resolve.JvmMethod
import org.jpascal.compiler.frontend.ir.types.Type

data class FunctionCall(
    val identifier: String,
    val arguments: List<Expression>,
    var resolved: JvmMethod? = null,
    override var type: Type? = null
) : Expression