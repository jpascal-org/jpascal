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
            access = Access.PUBLIC,
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

    @Test
    fun packageAndUses() {
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "Example.pas",
                """
                package org.company;
                
                uses a.b.C;
                uses a.b.d.*;
                uses a.b.d.x as y;
                
                begin
                    writeln('Hello World.');
                    readln;
                end.
                """.trimIndent()
            )
        )
        assertEquals("org.company", program.packageName)
        assertEquals(3, program.uses.size)
        assertEquals(Uses("a.b.C", null), program.uses[0])
        assertEquals(Uses("a.b.d.*", null), program.uses[1])
        assertEquals(Uses("a.b.d.x", "y"), program.uses[2])
    }

    @Test
    fun privateAndProtected() {
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "Example.pas",
                """
                private function foo(x, y: integer): integer;
                begin
                    foo := x + y;
                end;
                protected function bar(x, y: integer): integer;
                begin
                    bar := x + y;
                end;    
                """.trimIndent()
            )
        )
        assertEquals(Access.PRIVATE, program.declarations!!.functions[0].access)
        assertEquals(Access.PROTECTED, program.declarations!!.functions[1].access)
    }
}