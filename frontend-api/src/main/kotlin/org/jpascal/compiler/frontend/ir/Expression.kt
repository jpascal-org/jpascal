package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

sealed interface Expression : PositionedElement {
    val type: Type?
}