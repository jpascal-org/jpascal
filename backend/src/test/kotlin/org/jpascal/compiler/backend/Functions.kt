package org.jpascal.compiler.backend

import org.jpascal.compiler.backend.utils.ByteArrayClassLoader
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.Function
import org.jpascal.compiler.frontend.ir.types.IntegerType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class Functions {

    private fun dummyPosition(filename: String) =
        SourcePosition(filename, Position(0, 0), Position(0, 0))

    @Test
    fun returnIntegerLiteral() {
        val position = dummyPosition("ReturnIntegerLiteral.pas")
        val function = Function(
            name = "foo",
            params = listOf(),
            returnType = IntegerType,
            declarations = null,
            block = Block(
                operators = listOf(Assignment(Variable("foo"), IntegerLiteral(4)))
            ),
            position = position
        )
        val program = createProgram(position, function)
        val context = Context()
        val generator = ProgramGenerator(context)
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod(function.name).invoke(null)
        assertEquals(4, r)
    }

    @Test
    fun returnIntegerExpression() {
        val position = dummyPosition("ReturnIntegerExpression.pas")
        assertEquals(
            14, evalIntegerExpression(
                position, ArithmeticExpression(
                    ArithmeticOperation.PLUS,
                    IntegerLiteral(4),
                    IntegerLiteral(10)
                )
            )
        )
        assertEquals(
            20, evalIntegerExpression(
                position, ArithmeticExpression(
                    ArithmeticOperation.PLUS,
                    IntegerLiteral(6),
                    ArithmeticExpression(
                        ArithmeticOperation.PLUS,
                        IntegerLiteral(4),
                        IntegerLiteral(10)
                    )
                )
            )
        )
    }

    private fun evalIntegerExpression(position: SourcePosition, expression: Expression): Int {
        val function = Function(
            name = "foo",
            params = listOf(),
            returnType = IntegerType,
            declarations = null,
            block = Block(
                operators = listOf(
                    Assignment(
                        Variable("foo"),
                        expression
                    )
                )
            ),
            position = position
        )
        val program = createProgram(position, function)
        val context = Context()
        val generator = ProgramGenerator(context)
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        return clazz.getMethod(function.name).invoke(null) as Int
    }

    private fun createProgram(position: SourcePosition, function: Function) =
        Program(
            name = "MyProgram",
            uses = null,
            declarations = Declarations(
                functions = listOf(function),
                variables = listOf()
            ),
            block = Block(listOf()),
            position = position
        )

    private fun writeResult(result: CompilationResult) {
        File("/tmp/jpascal/${result.className}.class").writeBytes(result.bytecode)
    }

    private fun Map<String, ByteArray>.toClasses(): Map<String, Class<*>> {
        val loader = ByteArrayClassLoader(this)
        return mapValues { loader.loadClass(it.key) }
    }

    private fun List<CompilationResult>.toMap() = this.associate { it.className to it.bytecode }

    private fun CompilationResult.getClass(): Class<*> = listOf(this).toMap().toClasses()[this.className]!!
}