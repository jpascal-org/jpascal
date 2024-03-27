package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.common.Message
import org.jpascal.compiler.common.MessageLevel
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.ir.types.Type

data class ExpectedTypeMessage(
    val expected: Type,
    val found: Type,
    override val position: SourcePosition?
) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
}
