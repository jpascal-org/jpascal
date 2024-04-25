package org.jpascal.compiler.backend

import org.jpascal.compiler.frontend.ir.toJvmType
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.*
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type as AsmType
import org.objectweb.asm.commons.LocalVariablesSorter

class FunctionGenerator(
    private val mv: LocalVariablesSorter,
    private val function: FunctionDeclaration
) {

    private var maxStack = 0
    private var maxLocals = function.params.size + function.declarations.variables.size
    private val localVars = mutableMapOf<String, Int>()

    fun generate() {
        addFormalParametersToLocalVars()
        function.declarations.variables.forEach {
            val jvmType = it.type.toJvmType()
            val id = mv.newLocal(AsmType.getType(jvmType))
            localVars[it.name] = id
        }
        function.compoundStatement.statements.forEach(::generateStatement)
        if (function.returnType == UnitType) mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(maxStack, maxLocals)
    }

    private fun addFormalParametersToLocalVars() {
        val list = function.params
        var i = 0
        var jvmIndex = 0
        while (i < list.size) {
            localVars[list[i].name] = jvmIndex
            when (list[i++].type) {
                IntegerType, BooleanType -> jvmIndex++
                RealType -> jvmIndex += 2
            }
        }
    }

    private fun generateStatement(statement: Statement) {
        when (statement) {
            is AssignmentStatement -> generateAssignment(statement)
            is FunctionStatement -> generateFunctionCall(statement.functionCall)
            is ReturnStatement -> generateReturn(statement)
            is IfStatement -> generateIf(statement)
            is WhileStatement -> generateWhile(statement)
            is RepeatStatement -> generateRepeat(statement)
            is ForStatement -> generateFor(statement)
            is CompoundStatement -> statement.statements.forEach(::generateStatement)
            else -> TODO()
        }
    }

    private fun generateFor(statement: ForStatement) {
        generateAssignment(statement.variable, statement.initialValue)
        val variableType = statement.variable.type!!
        val finalValue = mv.newLocal(AsmType.getType(variableType.toJvmType()))
        generateExpression(statement.finalValue)
        storeVariable(finalValue, variableType)
        val start = Label()
        mv.visitLabel(start)
        loadVariable(statement.variable)
        loadVariable(finalValue, variableType)
        val exit = Label()
        if (statement.isDecrement) {
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, exit)
            generateStatement(statement.statement)
            decrement(statement.variable)
        } else {
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, exit)
            generateStatement(statement.statement)
            increment(statement.variable)
        }
        mv.visitJumpInsn(Opcodes.GOTO, start)
        mv.visitLabel(exit)
    }

    private fun increment(variable: Variable) {
        loadVariable(variable)
        when (variable.type) {
            IntegerType -> {
                mv.visitInsn(Opcodes.ICONST_1)
                mv.visitInsn(Opcodes.IADD)
            }

            else -> TODO()
        }
        storeVariable(variable)
    }

    private fun decrement(variable: Variable) {
        loadVariable(variable)
        when (variable.type) {
            IntegerType -> {
                mv.visitInsn(Opcodes.ICONST_1)
                mv.visitInsn(Opcodes.ISUB)
            }

            else -> TODO()
        }
        storeVariable(variable)
    }

    private fun generateRepeat(statement: RepeatStatement) {
        val start = Label()
        mv.visitLabel(start)
        generateStatement(statement.statement)
        val label = Label()
        generateBooleanExpression(statement.condition, label)
        mv.visitLabel(label)
        mv.visitInsn(Opcodes.ICONST_1)
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, start)
    }

    private fun generateWhile(statement: WhileStatement) {
        val start = Label()
        mv.visitLabel(start)
        val label = Label()
        generateBooleanExpression(statement.condition, label)
        mv.visitLabel(label)
        mv.visitInsn(Opcodes.ICONST_1)
        val exit = Label()
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, exit)
        generateStatement(statement.statement)
        mv.visitJumpInsn(Opcodes.GOTO, start)
        mv.visitLabel(exit)
    }

    private fun generateIf(statement: IfStatement) {
        val label = Label()
        generateBooleanExpression(statement.condition, label)
        mv.visitLabel(label)
        mv.visitInsn(Opcodes.ICONST_1)
        val elseLabel = Label()
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, elseLabel)
        generateStatement(statement.thenBranch)
        val exit = Label()
        mv.visitJumpInsn(Opcodes.GOTO, exit)
        mv.visitLabel(elseLabel)
        statement.elseBranch?.let(::generateStatement)
        mv.visitLabel(exit)
    }

    private fun generateBooleanExpression(expression: Expression, exit: Label) {
        fun generateWithExit(expression: Expression, exit: Label) {
            if (expression.type == BooleanType) {
                generateBooleanExpression(expression, exit)
            } else {
                generateExpression(expression)
            }
        }

        fun generateBooleanOp(left: Expression, right: Expression, inverseOp: Int) {
            val label1 = Label()
            generateWithExit(left, label1)
            mv.visitLabel(label1)
            val label2 = Label()
            generateWithExit(right, label2)
            mv.visitLabel(label2)
            val jumpIfFalse = Label()
            mv.visitJumpInsn(inverseOp, jumpIfFalse)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitJumpInsn(Opcodes.GOTO, exit)
            mv.visitLabel(jumpIfFalse)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitJumpInsn(Opcodes.GOTO, exit)
        }

        when (expression) {
            is BooleanLiteral ->
                if (expression.value) {
                    mv.visitInsn(Opcodes.ICONST_1)
                } else {
                    mv.visitInsn(Opcodes.ICONST_0)
                }

            is TreeExpression -> {
                val (op, left, right) = expression
                when (op) {
                    LogicalOperation.AND -> {
                        val label = Label()
                        generateBooleanExpression(left, label) // true or false on stack
                        mv.visitLabel(label)
                        mv.visitInsn(Opcodes.ICONST_0)
                        val next = Label()
                        mv.visitJumpInsn(Opcodes.IF_ICMPNE, next) // if true then check the right part
                        mv.visitInsn(Opcodes.ICONST_0)
                        mv.visitJumpInsn(Opcodes.GOTO, exit)
                        mv.visitLabel(next)
                        generateBooleanExpression(right, exit)
                    }

                    LogicalOperation.OR -> {
                        val label = Label()
                        generateBooleanExpression(left, label)
                        mv.visitLabel(label)
                        mv.visitInsn(Opcodes.ICONST_0)
                        val next = Label()
                        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, next)
                        mv.visitInsn(Opcodes.ICONST_1)
                        mv.visitJumpInsn(Opcodes.GOTO, exit)
                        mv.visitLabel(next)
                        generateBooleanExpression(right, exit)
                    }

                    LogicalOperation.XOR -> {
                        val label1 = Label()
                        generateBooleanExpression(left, label1)
                        mv.visitLabel(label1)
                        val label2 = Label()
                        generateBooleanExpression(right, label2)
                        mv.visitLabel(label2)
                        mv.visitInsn(Opcodes.IADD)
                        mv.visitInsn(Opcodes.ICONST_2)
                        mv.visitInsn(Opcodes.IREM) // not the best way but simple
                    }

                    RelationalOperation.EQ -> generateBooleanOp(left, right, Opcodes.IF_ICMPNE)
                    RelationalOperation.NEQ -> generateBooleanOp(left, right, Opcodes.IF_ICMPEQ)
                    RelationalOperation.GT -> generateBooleanOp(left, right, Opcodes.IF_ICMPLE)
                    RelationalOperation.LT -> generateBooleanOp(left, right, Opcodes.IF_ICMPGE)
                    RelationalOperation.GE -> generateBooleanOp(left, right, Opcodes.IF_ICMPLT)
                    RelationalOperation.LE -> generateBooleanOp(left, right, Opcodes.IF_ICMPGT)
                    else -> TODO("Op=$op")
                }
            }

            is UnaryExpression -> {
                // NOT
                mv.visitInsn(Opcodes.ICONST_1)
                val label = Label()
                generateBooleanExpression(expression.expression, label)
                mv.visitLabel(label)
                mv.visitInsn(Opcodes.ISUB)
            }

            else -> generateExpression(expression)
        }
    }

    private fun generateAssignment(statement: AssignmentStatement) =
        generateAssignment(statement.variable, statement.expression)

    private fun generateAssignment(variable: Variable, expression: Expression) {
        generateExpression(expression)
        storeVariable(variable)
    }

    private fun storeVariable(variable: Variable) {
        localVars[variable.name]?.let { id ->
            storeVariable(id, variable.type!!)
        } ?: variable.jvmField!!.let {
            mv.visitFieldInsn(Opcodes.PUTSTATIC, it.owner, it.name, it.descriptor)
        }
    }

    private fun loadVariable(variable: Variable) {
        localVars[variable.name]?.let { id ->
            loadVariable(id, variable.type!!)
        } ?: variable.jvmField!!.let {
            mv.visitFieldInsn(Opcodes.GETSTATIC, it.owner, it.name, it.descriptor)
        }
    }

    private fun storeVariable(id: Int, type: Type) {
        when (type) {
            IntegerType -> mv.visitVarInsn(Opcodes.ISTORE, id)
            RealType -> mv.visitVarInsn(Opcodes.DSTORE, id)
            else -> TODO()
        }
    }

    private fun loadVariable(id: Int, type: Type) {
        when (type) {
            IntegerType -> mv.visitVarInsn(Opcodes.ILOAD, id)
            RealType -> mv.visitVarInsn(Opcodes.DLOAD, id)
            else -> TODO()
        }
    }

    private fun generateFunctionCall(functionCall: FunctionCall) {
        val jvmSymbol = functionCall.resolved ?: throw UnresolvedSymbolError(functionCall.identifier)
        functionCall.arguments.forEach {
            generateExpression(it)
        }
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC, jvmSymbol.owner, jvmSymbol.name, jvmSymbol.descriptor, false
        )
    }

    private fun implicitConversion(source: Type, target: Type) {
        if (source == IntegerType && target == RealType) {
            mv.visitInsn(Opcodes.I2D)
        } else {
            TODO()
        }
    }

    private fun generateArithmetics(expression: TreeExpression, iop: Int, dop: Int) {
        generateExpression(expression.left)
        if (expression.left.type != expression.type) {
            implicitConversion(expression.left.type!!, expression.type!!)
        }
        generateExpression(expression.right)
        if (expression.right.type != expression.type) {
            implicitConversion(expression.right.type!!, expression.type!!)
        }
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
                val index = localVars[expression.name]
                if (index != null) {
                    when (expression.type) {
                        is IntegerType, BooleanType -> mv.visitVarInsn(Opcodes.ILOAD, index)
                        is RealType -> mv.visitVarInsn(Opcodes.DLOAD, index)
                        else -> TODO()
                    }
                } else {
                    val field = expression.jvmField!!
                    mv.visitFieldInsn(Opcodes.GETSTATIC, field.owner, field.name, field.descriptor)
                }
            }

            is TreeExpression -> when (expression.op) {
                ArithmeticOperation.PLUS -> generateArithmetics(expression, Opcodes.IADD, Opcodes.DADD)
                ArithmeticOperation.MINUS -> generateArithmetics(expression, Opcodes.ISUB, Opcodes.DSUB)
                ArithmeticOperation.TIMES -> generateArithmetics(expression, Opcodes.IMUL, Opcodes.DMUL)
                is RelationalOperation -> {
                    val label = Label()
                    generateBooleanExpression(expression, label)
                    mv.visitLabel(label)
                }

                else -> TODO("Op=${expression.op}")
            }

            is BooleanLiteral -> {
                val label = Label()
                generateBooleanExpression(expression, label)
                mv.visitLabel(label)
            }

            is FunctionCall -> {
                val jvmMethod = expression.resolved!!
                expression.arguments.forEach(::generateExpression)
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    jvmMethod.owner,
                    jvmMethod.name,
                    jvmMethod.descriptor,
                    false
                )
            }

            is StringLiteral -> mv.visitLdcInsn(expression.value)
            else -> TODO("Expression=$expression")
        }
    }

    private fun generateReturn(returnStatement: ReturnStatement) {
        returnStatement.expression?.let {
            generateExpression(it)
        }
        when (function.returnType) {
            IntegerType, BooleanType -> mv.visitInsn(Opcodes.IRETURN)
            RealType -> mv.visitInsn(Opcodes.DRETURN)
            else -> TODO()
        }
    }
}