package org.jpascal.compiler.backend

import org.jpascal.compiler.backend.utils.ByteArrayClassLoader
import org.jpascal.compiler.frontend.ir.*
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
        val function = FunctionDeclaration(
            identifier = "foo",
            params = listOf(),
            returnType = IntegerType,
            access = Access.PUBLIC,
            declarations = null,
            compoundStatement = CompoundStatement(
                statements = listOf(Assignment(Variable("foo", IntegerType), IntegerLiteral(4)))
            ),
            position = position
        )
        val program = createProgram(position, function)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod(function.identifier).invoke(null)
        assertEquals(4, r)
    }

    @Test
    fun returnIntegerExpression() {
        val position = dummyPosition("ReturnIntegerExpression.pas")
        assertEquals(
            14, evalIntegerExpression(
                position, TreeExpression(
                    ArithmeticOperation.PLUS,
                    IntegerLiteral(4),
                    IntegerLiteral(10),
                    IntegerType
                )
            )
        )
        assertEquals(
            20, evalIntegerExpression(
                position, TreeExpression(
                    ArithmeticOperation.PLUS,
                    IntegerLiteral(6),
                    TreeExpression(
                        ArithmeticOperation.PLUS,
                        IntegerLiteral(4),
                        IntegerLiteral(10),
                        IntegerType
                    ),
                    IntegerType
                )
            )
        )
    }

    private fun evalIntegerExpression(position: SourcePosition, expression: Expression): Int {
        val function = FunctionDeclaration(
            identifier = "foo",
            params = listOf(),
            returnType = IntegerType,
            access = Access.PUBLIC,
            declarations = null,
            compoundStatement = CompoundStatement(
                statements = listOf(
                    Assignment(
                        Variable("foo", IntegerType),
                        expression
                    )
                )
            ),
            position = position
        )
        val program = createProgram(position, function)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        return clazz.getMethod(function.identifier).invoke(null) as Int
    }

    @Test
    fun integerParameters() {
        val position = dummyPosition("IntegerParameters.pas")
        val function = FunctionDeclaration(
            identifier = "foo",
            params = listOf(
                FormalParameter("x", IntegerType, Pass.VALUE),
                FormalParameter("y", IntegerType, Pass.VALUE),
            ),
            returnType = IntegerType,
            access = Access.PUBLIC,
            declarations = null,
            compoundStatement = CompoundStatement(
                statements = listOf(
                    Assignment(
                        Variable("foo", IntegerType),
                        TreeExpression(
                            ArithmeticOperation.PLUS,
                            Variable("x", IntegerType),
                            Variable("y", IntegerType),
                            IntegerType
                        )
                    )
                )
            ),
            position = position
        )
        val program = createProgram(position, function)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod(function.identifier, Int::class.java, Int::class.java).invoke(null, 5, 10)
        assertEquals(15, r)
    }

    private fun createProgram(position: SourcePosition, function: FunctionDeclaration) =
        Program(
            packageName = "MyProgram",
            uses = listOf(),
            declarations = Declarations(
                functions = listOf(function),
                variables = listOf()
            ),
            compoundStatement = CompoundStatement(listOf()),
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