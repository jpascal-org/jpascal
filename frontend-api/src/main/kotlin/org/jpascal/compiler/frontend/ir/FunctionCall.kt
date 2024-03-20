package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

data class FunctionCall(
    val identifier: String,
    val arguments: List<Expression>,
    override var type: Type? = null
) : Expression