package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.FunctionCall
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.resolve.JvmMethod

class CannotMatchOverloadedCandidateMessage(
    val call: FunctionCall,
    val candidates: List<JvmMethod>
) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = call.position
}