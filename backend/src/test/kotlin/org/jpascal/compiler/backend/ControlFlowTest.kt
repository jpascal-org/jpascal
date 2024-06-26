package org.jpascal.compiler.backend

import kotlin.test.Test
import kotlin.test.assertEquals

class ControlFlowTest : BaseBackendTest() {
    @Test
    fun ifTrueStatement() {
        val method = compile(
            "IfTrueStatement.pas",
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
    fun ifStatement() {
        val method = compile(
            "IfStatement.pas",
            """
            function foo(x: boolean): integer;
            var result: integer;
            begin
                if x then 
                    result := 1
                else
                    result := 0;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Boolean::class.java)
        assertEquals(1, method.invoke(null, true))
        assertEquals(0, method.invoke(null, false))
    }

    @Test
    fun relationalOperations() {
        fun method(op: String) = compile(
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
        fun method(op: String) = compile(
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
        val method = compile(
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

    @Test
    fun shortCircuit() {
        fun myProgram(op: String) =
            compile(
                "ShortCircuit.pas",
                """
            var 
                touched: boolean;
                z: integer;
            function bar(y: boolean): boolean;
            begin
                touched := true;
                return y;
            end;
            procedure foo(x, y: boolean);
            begin
                if x $op bar(y) then
                    z := 1
                else
                    z := 0;
            end;
            """.trimIndent()
            )
        myProgram("and").let {
            val method = it.getMethod("foo", Boolean::class.java, Boolean::class.java)
            val touched = it.getDeclaredField("touched")
            touched.trySetAccessible()
            method.invoke(null, false, true)
            assertEquals(false, touched.getBoolean(null))
            method.invoke(null, true, false)
            assertEquals(true, touched.getBoolean(null))
        }
        myProgram("or").let {
            val method = it.getMethod("foo", Boolean::class.java, Boolean::class.java)
            val touched = it.getDeclaredField("touched")
            touched.trySetAccessible()
            method.invoke(null, true, false)
            assertEquals(false, touched.getBoolean(null))
            method.invoke(null, false, false)
            assertEquals(true, touched.getBoolean(null))
        }
    }

    @Test
    fun whileLoop() =
        compile(
            "WhileLoop.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                i := 1;
                result := 0;
                while i <= n do 
                begin
                    result := result + i;
                    i := i + 1;
                end;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun repeatUntilLoop() =
        compile(
            "RepeatUntilLoop.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                i := 1;
                result := 0;
                repeat
                    result := result + i;
                    i := i + 1;
                until i > n;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(1, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun forLoop() =
        compile(
            "ForLoop.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                result := 0;
                for i := 1 to n do 
                    result := result + i;
                return result;
            end;    
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun forDownToLoop() =
        compile(
            "ForDownToLoop.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                result := 0;
                for i := n downto 1 do 
                    result := result + i;
                return result;
            end;    
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun forLoopGlobalVar() =
        compile(
            "ForLoopGlobal.pas",
            """
            var i: integer;
            function foo(n: integer): integer;
            var
                result: integer;
            begin
                result := 0;
                for i := n downto 1 do 
                    result := result + i;
                return result;
            end;
            begin
            end.
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun whileLoopWithBreak() =
        compile(
            "WhileLoopWithBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                i := 1;
                result := 0;
                while i <= n + 5 do 
                begin
                    if i > n then break;
                    result := result + i;
                    i := i + 1;
                end;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }


    @Test
    fun whileLoopWithLabelAndBreak() =
        compile(
            "WhileLoopWithLabelAndBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                i := 1;
                result := 0;
                start: while i <= n + 5 do 
                begin
                    if i > n then break start;
                    result := result + i;
                    i := i + 1;
                end;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun nestedWhileWithBreak() =
        compile(
            "NestedLoopWithBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, j, result: integer;
            begin
                j := 1;
                result := 0;
                while j <= 2 do
                begin
                    i := 1;
                    while i <= n + 5 do 
                    begin
                        if i > n then break;
                        result := result + i;
                        i := i + 1;
                    end;
                    j := j + 1;
                end;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(2, it.invoke(null, 1))
            assertEquals(6, it.invoke(null, 2))
            assertEquals(12, it.invoke(null, 3))
        }

    @Test
    fun nestedWhileWithOuterBreak() =
        compile(
            "NestedLoopWithOuterBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, j, result: integer;
            begin
                j := 1;
                result := 0;
                outer: while j <= 4 do
                begin
                    i := 1;
                    while i <= n + 5 do 
                    begin
                        if i > n then break outer;
                        result := result + i;
                        i := i + 1;
                    end;
                    j := j + 1;
                end;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun forLoopWithBreak() =
        compile(
            "ForLoopWithBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                result := 0;
                for i := 1 to n + 5 do 
                begin
                    if i > n then break;
                    result := result + i;
                end;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun forLoopWithLabelAndBreak() =
        compile(
            "ForLoopWithLabelAndBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                i := 1;
                result := 0;
                start: for i := 1 to n + 5 do 
                begin
                    if i > n then break start;
                    result := result + i;
                end;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun repeatLoopWithBreak() =
        compile(
            "RepeatLoopWithBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                i := 1;
                result := 0;
                repeat
                    if i > n then break;
                    result := result + i;
                    i := i + 1;
                until i > n + 5;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }

    @Test
    fun repeatLoopWithLabelAndBreak() =
        compile(
            "RepeatLoopWithLabelAndBreak.pas",
            """
            function foo(n: integer): integer;
            var
                i, result: integer;
            begin
                i := 1;
                result := 0;
                start: repeat
                    if i > n then break start;
                    result := result + i;
                    i := i + 1;
                until i > n + 5;
                return result;
            end;
            """.trimIndent()
        ).getMethod("foo", Int::class.java).let {
            assertEquals(0, it.invoke(null, 0))
            assertEquals(1, it.invoke(null, 1))
            assertEquals(3, it.invoke(null, 2))
            assertEquals(6, it.invoke(null, 3))
        }
}