package org.jpascal.compiler.frontend

import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.ir.FunctionStatement
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.messages.CannotResolveFunctionMessage
import org.jpascal.compiler.frontend.resolve.Context
import org.jpascal.compiler.frontend.resolve.JvmMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        messageCollector.list().let { messages ->
            assertEquals(1, messages.size)
            assertTrue(messages[0] is CannotResolveFunctionMessage)
            assertEquals("writeln", (messages[0] as CannotResolveFunctionMessage).functionName)
        }
    }

    @Test
    fun multipleFiles() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program1 = parser.parse(
            Source(
                "Program1.pas",
                """
                function foo(x: integer): integer;
                begin
                    return bar(x);
                end;
                """.trimIndent()
            )
        )
        val program2 = parser.parse(
            Source(
                "Program2.pas",
                """
                function bar(x: integer): integer;
                begin
                    return foo(x);
                end;
                """.trimIndent()
            )
        )
        context.add(program1)
        context.add(program2)
        context.resolve(program1)
        context.resolve(program2)
        messageCollector.list().forEach {
            println(it)
        }
        assertEquals(0, messageCollector.list().size)
    }
}