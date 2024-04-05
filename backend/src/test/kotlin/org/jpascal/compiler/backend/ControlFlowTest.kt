package org.jpascal.compiler.backend

import kotlin.test.Test
import kotlin.test.assertEquals

class ControlFlowTest : BaseBackendTest() {
    @Test
    fun ifStatement() {
        val method = program(
            "IfStatement.pas",
            """
            function foo(x: integer): integer;
            begin
                if true then 
                    return 1
                else
                    return 0;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java)
        assertEquals(1, method.invoke(null, 10))
        assertEquals(1, method.invoke(null, 0))
    }

    @Test
    fun relationalOperations() {
        fun method(op: String) = program(
            "RelationalOperations.pas",
            """
            function foo(x: integer): integer;
            begin
                if x $op 0 then 
                    return 1
                else
                    return 0;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java)

        method(">").let {
            assertEquals(1, it.invoke(null, 10))
            assertEquals(0, it.invoke(null, 0))
            assertEquals(0, it.invoke(null, -10))
        }
        method(">=").let {
            assertEquals(1, it.invoke(null, 10))
            assertEquals(1, it.invoke(null, 0))
            assertEquals(0, it.invoke(null, -10))
        }
        method("<").let {
            assertEquals(0, it.invoke(null, 10))
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, -10))
        }
        method("<=").let {
            assertEquals(0, it.invoke(null, 10))
            assertEquals(1, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, -10))
        }
        method("=").let {
            assertEquals(0, it.invoke(null, 10))
            assertEquals(1, it.invoke(null, 0))
            assertEquals(0, it.invoke(null, -10))
        }
        method("<>").let {
            assertEquals(1, it.invoke(null, 10))
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, -10))
        }
    }

    @Test
    fun logicalOperations() {
        fun method(op: String) = program(
            "LogicalOperations.pas",
            """
            function foo(x: boolean; y: boolean): integer;
            begin
                if x $op y then 
                    return 1
                else
                    return 0;
            end;
            """.trimIndent()
        ).getMethod("foo", Boolean::class.java, Boolean::class.java)
        method("and").let {
            assertEquals(0, it.invoke(null, false, false))
            assertEquals(0, it.invoke(null, false, true))
            assertEquals(0, it.invoke(null, true, false))
            assertEquals(1, it.invoke(null, true, true))
        }
        method("or").let {
            assertEquals(0, it.invoke(null, false, false))
            assertEquals(1, it.invoke(null, false, true))
            assertEquals(1, it.invoke(null, true, false))
            assertEquals(1, it.invoke(null, true, true))
        }
        method("xor").let {
            assertEquals(0, it.invoke(null, false, false))
            assertEquals(1, it.invoke(null, false, true))
            assertEquals(1, it.invoke(null, true, false))
            assertEquals(0, it.invoke(null, true, true))
        }
    }

    @Test
    fun logicalNot() {
        val method = program(
            "LogicalOperations.pas",
            """
            function foo(x: boolean): integer;
            begin
                if not x then 
                    return 1
                else
                    return 0;
            end;
            """.trimIndent()
        ).getMethod("foo", Boolean::class.java)
        assertEquals(1, method.invoke(null, false))
        assertEquals(0, method.invoke(null, true))
    }
}