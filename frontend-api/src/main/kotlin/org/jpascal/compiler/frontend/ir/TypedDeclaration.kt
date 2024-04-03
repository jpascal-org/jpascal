package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

interface TypedDeclaration : PositionedElement {
    val type: Type
}