package org.jpascal.compiler.backend

import org.jpascal.compiler.backend.utils.ByteArrayClassLoader
import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.ir.types.RealType
import org.jpascal.compiler.frontend.parser.antlr.AntlrParserFacadeImpl
import org.jpascal.compiler.frontend.parser.api.ParserFacade
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.Context
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionsAndExpressionsTest {

    private fun dummyPosition(filename: String) =
        SourcePosition(filename, Position(0, 0), Position(0, 0))

    @Test
    fun returnIntegerNumber() {
        val position = dummyPosition("ReturnIntegerLiteral.pas")
        val function = FunctionDeclaration(
            identifier = "foo",
            params = listOf(),
            returnType = IntegerType,
            access = Access.PUBLIC,
            declarations = Declarations(),
            compoundStatement = CompoundStatement(
                statements = listOf(ReturnStatement(IntegerNumber(4)))
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
                    IntegerNumber(4),
                    IntegerNumber(10),
                    type = IntegerType
                )
            )
        )
        assertEquals(
            20, evalIntegerExpression(
                position, TreeExpression(
                    ArithmeticOperation.PLUS,
                    IntegerNumber(6),
                    TreeExpression(
                        ArithmeticOperation.PLUS,
                        IntegerNumber(4),
                        IntegerNumber(10),
                        type = IntegerType
                    ),
                    type = IntegerType
                )
            )
        )
    }

    private fun evalIntegerExpression(position: SourcePosition, expression: Expression): Int {
        val function = FunctionDeclaration(
            identifier = "foo",
            params = listOf(),
            returnType = expression.type!!,
            access = Access.PUBLIC,
            declarations = Declarations(),
            compoundStatement = CompoundStatement(
                statements = listOf(
                    ReturnStatement(
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
        val function = createFunction(
            TreeExpression(
                ArithmeticOperation.PLUS,
                Variable("x", type = IntegerType),
                Variable("y", type = IntegerType),
                type = IntegerType
            ), position
        )
        val program = createProgram(position, function)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod(function.identifier, Int::class.java, Int::class.java).invoke(null, 5, 10)
        assertEquals(15, r)
    }

    @Test
    fun integerArithmetics() {
        val position = dummyPosition("Arithmetics.pas")
        val x = Variable("x", type = IntegerType)
        val y = Variable("y", type = IntegerType)
        val function = createFunction(
            TreeExpression(
                ArithmeticOperation.TIMES,
                TreeExpression(ArithmeticOperation.PLUS, x, y, type = IntegerType),
                TreeExpression(ArithmeticOperation.MINUS, x, IntegerNumber(5), type = IntegerType),
                type = IntegerType
            ), position
        )
        val program = createProgram(position, function)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod(function.identifier, Int::class.java, Int::class.java).invoke(null, 10, 6)
        assertEquals((10 + 6) * (10 - 5), r)
    }

    @Test
    fun realArithmetics() {
        val position = dummyPosition("Arithmetics.pas")
        val x = Variable("x", type = RealType)
        val y = Variable("y", type = RealType)
        val function = createFunction(
            TreeExpression(
                ArithmeticOperation.TIMES,
                TreeExpression(ArithmeticOperation.PLUS, x, y, type = RealType),
                TreeExpression(ArithmeticOperation.MINUS, x, RealNumber(5.0), type = RealType),
                type = RealType
            ), position
        )
        val program = createProgram(position, function)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod(function.identifier, Double::class.java, Double::class.java).invoke(null, 10.0, 6.0)
        assertEquals((10.0 + 6.0) * (10.0 - 5.0), r)
    }

    @Test
    fun localIntegerVariables() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "LocalIntegerVariables.pas",
                """
                function foo(x, y: integer): integer;
                var
                    z, result: integer;
                begin
                    z := x + 2 * y;
                    result := z + z;
                    return result;
                end;
                """.trimIndent()
            )
        )
        context.resolve(program)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod("foo", Int::class.java, Int::class.java).invoke(null, 3, 6)
        val z = 3 + 2 * 6
        assertEquals(z + z, r)
    }

    @Test
    fun localRealVariables() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "LocalRealVariables.pas",
                """
                function foo(x, y: real): real;
                var
                    z, result: real;
                begin
                    z := x + 2.0 * y;
                    result := z + z;
                    return result;
                end;
                """.trimIndent()
            )
        )
        context.resolve(program)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod("foo", Double::class.java, Double::class.java).invoke(null, 3.0, 6.0)
        val z = 3.0 + 2 * 6.0
        assertEquals(z + z, r)
    }

    @Test
    fun integerToRealImplicitCast() {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(
            Source(
                "IntegerToRealImplicitCast.pas",
                """
                function foo(x, y: real): real;
                begin
                    return x + 2 * y;
                end;
                """.trimIndent()
            )
        )
        context.resolve(program)
        val generator = ProgramGenerator()
        val result = generator.generate(program)
        writeResult(result)
        val clazz = result.getClass()
        val r = clazz.getMethod("foo", Double::class.java, Double::class.java).invoke(null, 3.0, 6.0)
        assertEquals(3.0 + 2 * 6.0, r)
    }

    private fun createParserFacade(): ParserFacade = AntlrParserFacadeImpl()

    private fun createFunction(expression: Expression, position: SourcePosition): FunctionDeclaration {
        fun collectVariables(expression: Expression, acc: MutableList<Variable>) {
            when (expression) {
                is TreeExpression -> {
                    collectVariables(expression.left, acc)
                    collectVariables(expression.right, acc)
                }

                is Variable -> acc.add(expression)
                is FunctionCall -> {
                    expression.arguments.forEach { collectVariables(it, acc) }
                }
            }
        }

        val vars = mutableListOf<Variable>()
        collectVariables(expression, vars)
        return FunctionDeclaration(
            identifier = "foo",
            params = vars.toSet().sortedBy { it.name }.map { FormalParameter(it.name, it.type!!, Pass.VALUE, null) },
            returnType = expression.type!!,
            access = Access.PUBLIC,
            declarations = Declarations(),
            compoundStatement = CompoundStatement(
                statements = listOf(
                    ReturnStatement(
                        expression
                    )
                )
            ),
            position = position
        )
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