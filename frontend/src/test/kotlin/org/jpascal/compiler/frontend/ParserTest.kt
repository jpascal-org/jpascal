package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.ir.Access
import org.jpascal.compiler.frontend.ir.Position
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.ir.Uses
import org.jpascal.compiler.frontend.parser.api.Source
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
        fun mkPosition(start: Position, end: Position): SourcePosition =
            SourcePosition("SimpleFunction.pas", start, end)

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
}