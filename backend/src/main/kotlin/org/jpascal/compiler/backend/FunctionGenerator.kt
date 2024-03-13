package org.jpascal.compiler.backend

import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.Function
import org.objectweb.asm.MethodVisitor
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.objectweb.asm.Opcodes

class FunctionGenerator(
    private val context: Context,
    private val mv: MethodVisitor,
    private val function: Function) {

    private var maxStack = 0
    private var maxLocals = function.params.size

    fun generate() {
        function.block.operators.forEach {
            when (it) {
                is Assignment -> {
                    generateExpression(it.expression)
                    if (it.variable.name == function.name) {
                        generateReturn()
                    } else {
                        TODO()
                    }
                }
            }
        }
        mv.visitMaxs(maxStack, maxLocals)
    }

    private fun generateExpression(expression: Expression) {
        when (expression) {
            is IntegerLiteral -> when (expression.value) {
                0 -> mv.visitInsn(Opcodes.ICONST_0)
                1 -> mv.visitInsn(Opcodes.ICONST_1)
                2 -> mv.visitInsn(Opcodes.ICONST_2)
                3 -> mv.visitInsn(Opcodes.ICONST_3)
                4 -> mv.visitInsn(Opcodes.ICONST_4)
                5 -> mv.visitInsn(Opcodes.ICONST_5)
                else -> mv.visitIntInsn(Opcodes.BIPUSH, expression.value)
            }
            is ArithmeticExpression -> when (expression.op) {
                ArithmeticOperation.PLUS -> {
                    generateExpression(expression.left)
                    generateExpression(expression.right)
                    if (expression.type == IntegerType) {
                        mv.visitInsn(Opcodes.IADD)
                    } else {
                        TODO("Type=${expression.type}")
                    }
                }
                else -> TODO("Op=${expression.op}")
            }
            else -> TODO("Expression=$expression")
        }
    }

    private fun generateReturn() {
        when (function.returnType) {
            IntegerType -> mv.visitInsn(Opcodes.IRETURN)
            else -> TODO()
        }
    }
}