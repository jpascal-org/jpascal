package org.jpascal.compiler.frontend.ir

interface Statement : PositionedElement {
    var label: Label?
}