package org.jpascal.compiler.common

import org.jpascal.compiler.frontend.ir.SourcePosition

interface Message {
    val level: MessageLevel
    val position: SourcePosition?
}