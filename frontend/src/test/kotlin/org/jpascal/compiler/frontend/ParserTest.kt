package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.parser.api.Source
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParserTest : BaseFrontendTest() {
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
        fun mkPosition(start: Position, end: Position): SourcePosition =
            SourcePosition("SimpleFunction.pas", start, end)

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
                FormalParameter(name = "x", type = IntegerType, pass = Pass.VALUE,
                    position = mkPosition(Position(1, 14), Position(1, 15))),
                FormalParameter(name = "y", type = IntegerType, pass = Pass.VALUE,
                    position = mkPosition(Position(1, 17), Position(1, 18)))
            ),
            returnType = IntegerType,
            access = Access.PUBLIC,
            declarations = Declarations(),
            compoundStatement = CompoundStatement(
                listOf(
                    Assignment(
                        variable = Variable(
                            "foo",
                            position = mkPosition(Position(3, 5), Position(3, 8))
                        ),
                        expression = TreeExpression(
                            op = ArithmeticOperation.PLUS,
                            left = Variable("x", position = mkPosition(Position(3, 12), Position(3, 13))),
                            right = Variable("y", position = mkPosition(Position(3, 16), Position(3, 17))),
                            position = mkPosition(Position(3, 12), Position(3, 17))
                        ),
                        position = mkPosition(Position(3, 5), Position(3, 17))
                    )
                ),
                position = mkPosition(Position(2, 1), Position(4, 4))
            ),
            position = SourcePosition(
                "SimpleFunction.pas",
                start = Position(1, 1), end = Position(4, 4)
            )
        )
        assertEquals(func, program.declarations.functions[0])
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
        assertEquals(Access.PRIVATE, program.declarations.functions[0].access)
        assertEquals(Access.PROTECTED, program.declarations.functions[1].access)
    }
}