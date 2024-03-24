package org.jpascal.compiler.frontend

import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.ir.FunctionStatement
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.Context
import org.jpascal.compiler.frontend.resolve.JvmMethod
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveTest : BaseFrontendTest() {
    @Test
    fun resolveWriteln() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        context.addSystemLibrary("org.jpascal.stdlib.PreludeKt")
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "HelloWorld.pas",
                """
                begin
                    writeln('Hello World.');
                end.
                """.trimIndent()
            )
        )
        context.resolve(program)
        val stmt = program.compoundStatement.statements[0] as FunctionStatement
        assertEquals(
            JvmMethod(
                owner = "org/jpascal/stdlib/PreludeKt",
                name = "writeln",
                descriptor = "(Ljava/lang/String;)V"
            ), stmt.functionCall.resolved
        )
    }

    @Test
    fun unresolvedFunction() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "HelloWorld.pas",
                """
                begin
                    writeln('Hello World.');
                end.
                """.trimIndent()
            )
        )
        context.resolve(program)
        messageCollector.getMessages().let { messages ->
            assertEquals(1, messages.size)
            assertEquals(FrontendMessages.CANNOT_RESOLVE_FUNCTION, messages[0].code)
            assertEquals("writeln", messages[0].arguments[0])
        }
    }
}