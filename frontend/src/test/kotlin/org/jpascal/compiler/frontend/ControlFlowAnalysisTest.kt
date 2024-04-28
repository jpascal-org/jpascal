package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.controlflow.messages.MissingReturnStatementMessage
import org.jpascal.compiler.frontend.resolve.messages.BreakIsOutOfLoopMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ControlFlowAnalysisTest : BaseFrontendTest() {
    @Test
    fun missingReturnInFunction() =
        resolve(
            "MissingReturnInFunction.pas",
            """
                function foo: integer;
                begin
                end;
                """.trimIndent()
        ).let {
            assertEquals(1, it.list().size)
            assertTrue(it.list()[0] is MissingReturnStatementMessage)
        }

    @Test
    fun noReturnInProcedure() =
        resolve(
            "NoReturnInProcedure.pas",
            """
                procedure foo;
                begin
                end;
                """.trimIndent()
        ).let {
            assertEquals(0, it.list().size)
        }

    @Test
    fun missingReturnInThenBranch() =
        resolve(
            "MissingReturnInThenBranch.pas",
            """
                function foo: integer;
                var
                    x: integer;
                begin
                    if false then
                        x := 1
                    else
                        return 1;
                end;
                """.trimIndent()
        ).let {
            assertEquals(1, it.list().size)
            assertTrue(it.list()[0] is MissingReturnStatementMessage)
        }

    @Test
    fun missingReturnInElseBranch() =
        resolve(
            "MissingReturnInElseBranch.pas",
            """
                function foo: integer;
                var
                    x: integer;
                begin
                    if false then
                        return 1
                    else
                        x := 1;
                end;
                """.trimIndent()
        ).let {
            assertEquals(1, it.list().size)
            assertTrue(it.list()[0] is MissingReturnStatementMessage)
        }

    @Test
    fun missingReturnNoElse() =
        resolve(
            "MissingReturnNoElse.pas",
            """
                function foo: integer;
                var
                    x: integer;
                begin
                    if false then return 1;
                end;
                """.trimIndent()
        ).let {
            assertEquals(1, it.list().size)
            assertTrue(it.list()[0] is MissingReturnStatementMessage)
        }

    @Test
    fun globalReturnWithoutReturnInElseBranch() =
        resolve(
            "GlobalReturnWithoutReturnInElseBranch.pas",
            """
                function foo: integer;
                var
                    x: integer;
                begin
                    if false then
                        return 1
                    else
                        x := 1;
                    return 0;
                end;
                """.trimIndent()
        ).let {
            assertEquals(0, it.list().size)
        }

    @Test
    fun breakIsOutOfLoop() =
        resolve(
        "BreakIsOutOfLoop.pas",
        """
            function foo(x: integer): integer;
            begin
                if true then 
                    break
                else
                    return 0;
                return 1;
            end;
            """.trimIndent()
        ).let {
            assertEquals(1, it.list().size)
            assertTrue(it.list()[0] is BreakIsOutOfLoopMessage)
        }
}