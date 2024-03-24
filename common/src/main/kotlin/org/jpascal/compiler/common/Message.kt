package org.jpascal.compiler.common

import org.jpascal.compiler.frontend.ir.SourcePosition

data class Message(val code: MessageCode, val level: MessageLevel, val position: SourcePosition?, val arguments: List<Any?>) {
    constructor(code: MessageCode, level: MessageLevel, position: SourcePosition?, vararg arguments: Any?) :
            this(code, level, position, arguments.toList())
}