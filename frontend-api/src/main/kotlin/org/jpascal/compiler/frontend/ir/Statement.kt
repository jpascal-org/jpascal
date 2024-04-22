package org.jpascal.compiler.frontend.ir

interface Statement : PositionedElement {
    val label: Label?
}