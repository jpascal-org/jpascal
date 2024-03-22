package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.ir.FunctionStatement
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.JvmMethod
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveTest : BaseFrontendTest() {
    @Test
    fun resolveWriteln() {
        val context = Context()
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
}