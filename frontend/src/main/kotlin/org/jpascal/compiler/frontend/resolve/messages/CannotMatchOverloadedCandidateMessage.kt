package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.common.Message
import org.jpascal.compiler.common.MessageLevel
import org.jpascal.compiler.frontend.ir.FunctionCall
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.resolve.JvmMethod

class CannotMatchOverloadedCandidateMessage(
    val call: FunctionCall,
    val candidates: List<JvmMethod>,
    override val position: SourcePosition?
) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
}