package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.Expression
import org.jpascal.compiler.frontend.ir.Lvalue
import org.jpascal.compiler.frontend.ir.SourcePosition

data class LvalueTypeIsNotAssignableMessage(val lvalue: Lvalue, val expression: Expression) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = lvalue.position
}