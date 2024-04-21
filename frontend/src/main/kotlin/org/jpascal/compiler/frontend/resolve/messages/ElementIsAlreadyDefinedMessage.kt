package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.PositionedElement
import org.jpascal.compiler.frontend.ir.SourcePosition

data class ElementIsAlreadyDefinedMessage(
    val element: PositionedElement,
    val defined: PositionedElement
) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = element.position
}