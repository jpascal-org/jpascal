package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

interface Expression {
    val type: Type?
}