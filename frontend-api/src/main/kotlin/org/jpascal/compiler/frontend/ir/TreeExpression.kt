package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

data class TreeExpression(
    val op: Operation,
    val left: Expression,
    val right: Expression,
    override var type: Type? = null
) : Expression