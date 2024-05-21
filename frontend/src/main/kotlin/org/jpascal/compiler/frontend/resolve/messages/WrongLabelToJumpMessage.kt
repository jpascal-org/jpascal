package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.JumpStatement
import org.jpascal.compiler.frontend.ir.SourcePosition

data class WrongLabelToJumpMessage(
    val statement: JumpStatement,
    override val position: SourcePosition?
) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
}