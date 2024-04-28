package org.jpascal.compiler.frontend.ir

interface PositionedElement {
    var parent: PositionedElement?
    val position: SourcePosition?
}