package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.common.Message
import org.jpascal.compiler.common.MessageLevel
import org.jpascal.compiler.frontend.ir.SourcePosition

data class ProcedureCannotReturnValueMessage(override val position: SourcePosition?) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
}