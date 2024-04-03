package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.common.Message
import org.jpascal.compiler.common.MessageLevel
import org.jpascal.compiler.frontend.ir.FunctionCall
import org.jpascal.compiler.frontend.ir.SourcePosition

data class CannotResolveFunctionMessage(
    val call: FunctionCall
) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = call.position
}