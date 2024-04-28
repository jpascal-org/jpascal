package org.jpascal.compiler.frontend.ir

sealed interface Statement : PositionedElement {
    var label: Label?
}