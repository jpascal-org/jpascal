package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.ir.types.Type

class ExpectedExpressionTypeMessage(val expected: List<Type>, val foundType: Type, override val position: SourcePosition?) :
    Message {
    override val level: MessageLevel = MessageLevel.ERROR
}