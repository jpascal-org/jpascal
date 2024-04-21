package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.ir.SourcePosition

interface Message {
    val level: MessageLevel
    val position: SourcePosition?
}