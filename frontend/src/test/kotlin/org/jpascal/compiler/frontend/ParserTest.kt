package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.parser.api.Source
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParserTest : BaseFrontendTest()  {
    @Test
    fun helloWorld() {
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "HelloWorld.pas",
                """
                program HelloWorld;
                begin
                    writeln('Hello World.');
                    readln;
                end.
                """.trimIndent()
            )
        )
        println(program)
    }

    @Test
    fun simpleFunction() {
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "SimpleFunction.pas",
                """
                function foo(x, y: integer): integer;
                begin
                    foo := x + y;
                end;
                """.trimIndent()
            )
        )
        assertNotNull(program.declarations)
        val func = FunctionDeclaration(
            identifier = "foo",
            params = listOf(
                FormalParameter(name = "x", type = IntegerType, pass = Pass.VALUE),
                FormalParameter(name = "y", type = IntegerType, pass = Pass.VALUE)
            ),
            returnType = IntegerType,
            declarations = null,
            compoundStatement = CompoundStatement(
                listOf(
                    Assignment(
                        variable = Variable("foo"),
                        expression = TreeExpression(
                            op = ArithmeticOperation.PLUS,
                            left = Variable("x"),
                            right = Variable("y")
                        )
                    )
                )
            ),
            position = SourcePosition(
                "SimpleFunction.pas",
                start = Position(1, 1), end = Position(4, 4)
            )
        )
        assertEquals(func, program.declarations!!.functions[0])
    }
}