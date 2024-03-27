package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

interface Expression : PositionedElement {
    val type: Type?
}