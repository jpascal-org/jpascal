package org.jpascal.compiler.frontend.ir

data class SourcePosition(val filename: String, val start: Position, val end: Position)