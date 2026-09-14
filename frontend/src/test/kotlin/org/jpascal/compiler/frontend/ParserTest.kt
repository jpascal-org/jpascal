package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.ArrayType
import org.jpascal.compiler.frontend.ir.types.RawRangeType
import org.jpascal.compiler.frontend.parser.api.Source
import kotlin.test.*

class ParserTest : BaseFrontendTest() {
    @Test
    fun helloWorld() {
        val messageCollector = MessageCollector()
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
            ), messageCollector
        )
        println(program)
    }

    @Test
    fun simpleFunction() {
        val messageCollector = MessageCollector()
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
            ), messageCollector
        )
        assertNotNull(program.declarations)
        assertEquals("foo", program.declarations.functions[0].identifier)
    }

    @Test
    fun packageAndUses() {
        val messageCollector = MessageCollector()
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
            ), messageCollector
        )
        assertEquals("org.company", program.packageName)
        assertEquals(3, program.uses.size)
        assertEquals(Uses("a.b.C", null), program.uses[0])
        assertEquals(Uses("a.b.d.*", null), program.uses[1])
        assertEquals(Uses("a.b.d.x", "y"), program.uses[2])
    }

    @Test
    fun privateAndProtected() {
        val messageCollector = MessageCollector()
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
            ), messageCollector
        )
        assertEquals(Access.PRIVATE, program.declarations.functions[0].access)
        assertEquals(Access.PROTECTED, program.declarations.functions[1].access)
    }

    @Test
    fun `one-dimensional array with range`() {
        val messageCollector = MessageCollector()
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "ArrayWithRange.pas",
                """
                private function foo(x, y: integer): integer;
                var
                    a: array[0..10] of integer;
                begin
                    a[0] := x + y;
                    return a[0];
                end;
                """.trimIndent()
            ), messageCollector
        )
        val arrayType = program.declarations.functions[0].declarations.variables[0].type
        assertTrue(arrayType is ArrayType)
        assertEquals(1, arrayType.indexTypes.size)
        val rangeType = arrayType.indexTypes[0]
        assertTrue(rangeType is RawRangeType)
        assertEquals(0, rangeType.min)
        assertEquals(10, rangeType.max)
        val statement = program.declarations.functions[0].compoundStatement.statements[0]
        assertTrue(statement is AssignmentStatement)
        val lhs = statement.LValue
        assertTrue(lhs is ArrayElement)
        assertEquals("a", lhs.name)
        assertEquals(1, lhs.indices.size)
        assertTrue(lhs.indices[0] is IntegerNumber)
        assertEquals(0, (lhs.indices[0] as IntegerNumber).value)
    }

    @Test
    fun `two-dimensional array with range`() {
        val messageCollector = MessageCollector()
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "ArrayWithRange.pas",
                """
                private function foo(x, y: integer): integer;
                var
                    a: array[0..10, -1..1] of integer;
                begin
                    a[0, -1] := x + y;
                    return a[0, -1];
                end;
                """.trimIndent()
            ), messageCollector
        )
        val arrayType = program.declarations.functions[0].declarations.variables[0].type
        assertTrue(arrayType is ArrayType)
        assertEquals(2, arrayType.indexTypes.size)
        arrayType.indexTypes[0].let { rangeType ->
            assertTrue(rangeType is RawRangeType)
            assertEquals(0, rangeType.min)
            assertEquals(10, rangeType.max)
        }
        arrayType.indexTypes[1].let { rangeType ->
            assertTrue(rangeType is RawRangeType)
            assertEquals(-1, rangeType.min)
            assertEquals(1, rangeType.max)
        }
        val statement = program.declarations.functions[0].compoundStatement.statements[0]
        assertTrue(statement is AssignmentStatement)
        val lhs = statement.LValue
        assertTrue(lhs is ArrayElement)
        assertEquals("a", lhs.name)
        assertEquals(2, lhs.indices.size)
        assertTrue(lhs.indices[0] is IntegerNumber)
        assertTrue(lhs.indices[1] is UnaryExpression)
        assertEquals(0, (lhs.indices[0] as IntegerNumber).value)
        assertTrue(lhs.indices[1] is UnaryExpression)
        (lhs.indices[1] as UnaryExpression).let { expression ->
            assertEquals(ArithmeticOperation.UNARY_MINUS, expression.op)
            assertTrue(expression.expression is IntegerNumber)
            assertEquals(1, (expression.expression as IntegerNumber).value)
        }
    }
}