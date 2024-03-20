package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

data class UnaryExpression(
    val op: Operation,
    val expression: Expression,
    override var type: Type? = null
) : Expression