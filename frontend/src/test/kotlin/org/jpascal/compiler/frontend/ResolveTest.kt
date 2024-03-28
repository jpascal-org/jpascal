package org.jpascal.compiler.frontend

import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.ir.FunctionStatement
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.ir.types.RealType
import org.jpascal.compiler.frontend.ir.types.StringType
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.Context
import org.jpascal.compiler.frontend.resolve.JvmMethod
import org.jpascal.compiler.frontend.resolve.messages.*
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

    @Test
    fun undefinedVariable() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "UndefinedVariable.pas",
                """
                begin
                    x := 'Hello';
                end.
                """.trimIndent()
            )
        )
        context.resolve(program)
        messageCollector.list().let { messages ->
            assertEquals(1, messages.size)
            assertTrue(messages[0] is VariableIsNotDefinedMessage)
            assertEquals("x", (messages[0] as VariableIsNotDefinedMessage).name)
        }
    }

    @Test
    fun typeIsNotAssignableFrom() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "typeIsNotAssignableFrom.pas",
                """
                var x : integer;     
                begin
                    x := 'Hello';
                end.
                """.trimIndent()
            )
        )
        println(program)
        context.resolve(program)
        messageCollector.list().let { messages ->
            assertEquals(1, messages.size)
            assertTrue(messages[0] is TypeIsNotAssignableMessage)
            val message = (messages[0] as TypeIsNotAssignableMessage)
            assertEquals(IntegerType, message.lhsType)
            assertEquals(StringType, message.rhsType)
        }
    }

    @Test
    fun incompatibleReturnType() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "IncompatibleReturnType.pas",
                """
                function foo(x: real): integer;
                begin
                    return x;
                end;
                """.trimIndent()
            )
        )
        context.add(program)
        context.resolve(program)
        messageCollector.list().let {
            assertEquals(1, it.size)
            assertTrue(it[0] is TypeIsNotAssignableMessage)
            val message = it[0] as TypeIsNotAssignableMessage
            assertEquals(IntegerType, message.lhsType)
            assertEquals(RealType, message.rhsType)
        }
    }

    @Test
    fun missingReturnExpression() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "IncompatibleReturnType.pas",
                """
                function foo: integer;
                begin
                    return;
                end;
                """.trimIndent()
            )
        )
        context.add(program)
        context.resolve(program)
        messageCollector.list().let {
            assertEquals(1, it.size)
            assertTrue(it[0] is ExpectedReturnValueMessage)
            val message = it[0] as ExpectedReturnValueMessage
            assertEquals(IntegerType, message.expected)
        }
    }

    @Test
    fun procedureCannotReturnValue() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "ProcedureCannotReturnValue.pas",
                """
                procedure foo;
                begin
                    return 2;
                end;
                """.trimIndent()
            )
        )
        context.add(program)
        context.resolve(program)
        messageCollector.list().let {
            it.forEach { println(it) }
            assertEquals(1, it.size)
            assertTrue(it[0] is ProcedureCannotReturnValueMessage)
        }
    }

}