package org.jpascal.compiler.backend

import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.types.BooleanType
import org.objectweb.asm.MethodVisitor
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.ir.types.RealType
import org.objectweb.asm.Opcodes

class FunctionGenerator(
    private val mv: MethodVisitor,
    private val function: FunctionDeclaration
) {

    private var maxStack = 0
    private var maxLocals = function.params.size

    fun generate() {
        function.compoundStatement.statements.forEach(::generateStatement)
        mv.visitMaxs(maxStack, maxLocals)
    }

    private fun generateStatement(statement: Statement) {
        when (statement) {
            is Assignment -> {
                TODO()
            }

            is FunctionStatement -> generateFunctionCall(statement.functionCall)
            is ReturnStatement -> {
                statement.expression?.let {
                    generateExpression(it)
                }
                generateReturn()
            }
        }
    }

    private fun generateFunctionCall(functionCall: FunctionCall) {
        val jvmSymbol = functionCall.resolved!!
        functionCall.arguments.forEach {
            generateExpression(it)
        }
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            jvmSymbol.owner,
            jvmSymbol.name,
            jvmSymbol.descriptor,
            false
        )
    }

    private fun List<FormalParameter>.findIndexByName(name: String): Int? {
        var i = 0
        var jvmIndex = 0
        while (i < size) {
            if (this[i].name == name) return jvmIndex
            when (this[i++].type) {
                IntegerType, BooleanType -> jvmIndex++
                RealType -> jvmIndex += 2
            }
        }
        return null
    }

    private fun generateArithmetics(expression: TreeExpression, iop: Int, dop: Int) {
        generateExpression(expression.left)
        generateExpression(expression.right)
        when (expression.type) {
            IntegerType -> mv.visitInsn(iop)
            RealType -> mv.visitInsn(dop)
            else -> TODO("Type=${expression.type}")
        }
    }

    private fun generateExpression(expression: Expression) {
        when (expression) {
            is IntegerNumber -> when (expression.value) {
                0 -> mv.visitInsn(Opcodes.ICONST_0)
                1 -> mv.visitInsn(Opcodes.ICONST_1)
                2 -> mv.visitInsn(Opcodes.ICONST_2)
                3 -> mv.visitInsn(Opcodes.ICONST_3)
                4 -> mv.visitInsn(Opcodes.ICONST_4)
                5 -> mv.visitInsn(Opcodes.ICONST_5)
                else -> mv.visitIntInsn(Opcodes.BIPUSH, expression.value)
            }

            is RealNumber -> when (expression.value) {
                0.0 -> mv.visitInsn(Opcodes.DCONST_0)
                1.0 -> mv.visitInsn(Opcodes.DCONST_1)
                else -> mv.visitLdcInsn(expression.value)
            }

            is Variable -> {
                val index = function.params.findIndexByName(expression.name)
                if (index != null) {
                    when (expression.type) {
                        is IntegerType -> mv.visitVarInsn(Opcodes.ILOAD, index)
                        is RealType -> mv.visitVarInsn(Opcodes.DLOAD, index)
                        else -> TODO()
                    }
                } else {
                    TODO()
                }
            }

            is TreeExpression -> when (expression.op) {
                ArithmeticOperation.PLUS -> generateArithmetics(expression, Opcodes.IADD, Opcodes.DADD)
                ArithmeticOperation.MINUS -> generateArithmetics(expression, Opcodes.ISUB, Opcodes.DSUB)
                ArithmeticOperation.TIMES -> generateArithmetics(expression, Opcodes.IMUL, Opcodes.DMUL)
                else -> TODO("Op=${expression.op}")
            }

            else -> TODO("Expression=$expression")
        }
    }

    private fun generateReturn() {
        when (function.returnType) {
            IntegerType -> mv.visitInsn(Opcodes.IRETURN)
            RealType -> mv.visitInsn(Opcodes.DRETURN)
            else -> TODO()
        }
    }
}