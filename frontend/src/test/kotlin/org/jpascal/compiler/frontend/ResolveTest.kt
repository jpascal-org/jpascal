package org.jpascal.compiler.frontend

import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.ir.*
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
import kotlin.test.fail

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
            assertEquals("writeln", (messages[0] as CannotResolveFunctionMessage).call.identifier)
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
            assertEquals("x", (messages[0] as VariableIsNotDefinedMessage).variable.name)
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

    @Test
    fun overloadedFunctions() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "OverloadedFunctions.pas",
                """
                function foo(x: integer; y: real): integer;
                begin
                    return 1;
                end;
                function foo(x: real; y: integer): integer;
                begin
                    return 2;
                end;
                var x: integer;
                begin
                    x := foo(0.1, 2);
                end.
                """.trimIndent()
            )
        )
        context.add(program)
        context.resolve(program)
        assertEquals(0, messageCollector.list().size)
        val call = ((program.compoundStatement.statements[0] as Assignment).expression as FunctionCall).resolved
        val func = program.declarations.functions[1].jvmMethod
        assertEquals(func, call)
    }

    @Test
    fun cannotMatchOverloadedCandidate() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "CannotMatchOverloadedCandidates.pas",
                """
                function foo(x: integer; y: real): integer;
                begin
                    return 1;
                end;
                function foo(x: real; y: integer): integer;
                begin
                    return 2;
                end;
                var x: integer;
                begin
                    x := foo(0.1, 2.0);
                end.
                """.trimIndent()
            )
        )
        context.add(program)
        context.resolve(program)
        messageCollector.list().let {
            assertEquals(1, it.size)
            assertTrue(it[0] is CannotMatchOverloadedCandidateMessage)
            val message = it[0] as CannotMatchOverloadedCandidateMessage
            assertEquals(2, message.candidates.size)
        }
    }

    @Test
    fun duplicateVariableDeclarationInFunction() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "DuplicateVariableDeclarationInFunction.pas",
                """
                function foo(x: integer; y: real; y: integer): real;
                var 
                    x: real;
                    z, z: integer;
                begin
                    return x + z;
                end;
                """.trimIndent()
            )
        )
        context.add(program)
        context.resolve(program)
        messageCollector.list().let {
            assertEquals(3, it.size)
            (it[0] as? ElementIsAlreadyDefinedMessage)?.let { msg ->
                (msg.defined as? FormalParameter)?.let { param ->
                    assertEquals("y", param.name)
                } ?: fail()
                (msg.element as? FormalParameter)?.let { decl ->
                    assertEquals("y", decl.name)
                } ?: fail()
            } ?: fail()
            (it[1] as? ElementIsAlreadyDefinedMessage)?.let { msg ->
                (msg.defined as? FormalParameter)?.let { param ->
                    assertEquals("x", param.name)
                } ?: fail()
                (msg.element as? VariableDeclaration)?.let { decl ->
                    assertEquals("x", decl.name)
                } ?: fail()
            } ?: fail()
            (it[2] as? ElementIsAlreadyDefinedMessage)?.let { msg ->
                (msg.defined as? VariableDeclaration)?.let { decl ->
                    assertEquals("z", decl.name)
                } ?: fail()
                (msg.element as? VariableDeclaration)?.let { decl ->
                    assertEquals("z", decl.name)
                } ?: fail()
            } ?: fail()
        }

    }
}