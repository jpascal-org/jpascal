package org.jpascal.compiler.frontend.resolve

import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.common.ir.getJvmClassName
import org.jpascal.compiler.common.ir.getJvmDescriptor
import org.jpascal.compiler.common.ir.globalVariableJvmField
import org.jpascal.compiler.common.ir.toJvmType
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.*
import org.jpascal.compiler.frontend.resolve.messages.*
import org.objectweb.asm.ClassReader

class Context(private val messageCollector: MessageCollector) {
    private val externalFunctions = mutableMapOf<String, MutableList<JvmMethod>>()

    private fun JvmMethod.getTypeSignature(): String = descriptor.substring(1, descriptor.indexOf(')'))
    private fun FunctionCall.getTypeSignature(): String =
        arguments
            .map { it.type?.toJvmType() }
            .joinToString(separator = "")

    private fun JvmMethod.matchSignature(call: FunctionCall): Boolean {
        return getTypeSignature() == call.getTypeSignature()
    }

    private fun JvmMethod.getReturnType(): Type =
        when (val returnType = descriptor.substring(descriptor.indexOf(')') + 1)) {
            "I" -> IntegerType
            "D" -> RealType
            "V" -> UnitType
            else -> TODO("type=$returnType")
        }

    fun add(program: Program) {
        // TODO: add uses
        program.declarations.functions.forEach { func ->
            val className = program.getJvmClassName()
            val jvmMethod = JvmMethod(
                owner = program.packageName?.let { "$it.$className" } ?: className,
                name = func.identifier,
                descriptor = func.getJvmDescriptor()
            )
            func.jvmMethod = jvmMethod
            addToExternal(func.identifier, jvmMethod)
        }
    }

    private fun addToExternal(functionName: String, jvmMethod: JvmMethod) {
        val externals = externalFunctions.getOrPut(functionName) { mutableListOf() }
        externals.find { it.getTypeSignature() == jvmMethod.getTypeSignature() }?.let {
            messageCollector.add(FunctionIsAlreadyDefinedMessage(functionName, it, null))
            return
        }
        externals.add(jvmMethod)
    }

    fun addSystemLibrary(className: String) {
        val cr = ClassReader(className)
        val cv = CollectStaticMethodsClassVisitor()
        cr.accept(cv, 0)
        cv.listMethods().forEach {
            addToExternal(it.name, it)
        }
    }

    private fun resolve(functionCall: FunctionCall, scope: Scope) {
        functionCall.arguments.forEach { resolve(it, scope) }
        scope.findFunctionCandidates(functionCall.identifier)?.let { candidates ->
            if (candidates.size == 1) {
                functionCall.resolved = candidates[0]
                functionCall.type = candidates[0].getReturnType()
            } else {
                candidates.find { method -> method.matchSignature(functionCall) }?.let { method ->
                    functionCall.resolved = method
                    functionCall.type = method.getReturnType()
                } ?: messageCollector.add(
                    CannotMatchOverloadedCandidateMessage(functionCall, candidates)
                )
            }
        } ?: messageCollector.add(CannotResolveFunctionMessage(functionCall))
    }

    private fun resolve(variable: Variable, scope: Scope) {
        val declarationScope = scope.findVariableDeclarationScope(variable.name)
        val decl = declarationScope?.findVariable(variable.name)
        if (decl == null) {
            messageCollector.add(VariableIsNotDefinedMessage(variable))
        } else {
            variable.type = decl.type
            if (declarationScope.parent == null) variable.jvmField = (decl as VariableDeclaration).globalVariableJvmField()
        }
    }

    private fun resolve(expression: Expression, scope: Scope) {
        when (expression) {
            is FunctionCall -> resolve(expression, scope)
            is TreeExpression -> {
                resolve(expression.left, scope)
                resolve(expression.right, scope)
                expression.type = getExpressionType(expression)
            }

            is Variable -> resolve(expression, scope)
        }
//        if (expression.type == null) TODO("expr=$expression")
    }

    private fun Type.isNumeric() =
        when (this) {
            is IntType -> true
            is RealType -> true
            else -> false
        }

    private fun getExpressionType(expression: TreeExpression): Type? {
        val (op, left, right) = expression
        val leftType = left.type ?: return null
        val rightType = right.type ?: return null
        return when (op) {
            ArithmeticOperation.PLUS,
            ArithmeticOperation.MINUS,
            ArithmeticOperation.TIMES -> {
                if (!leftType.isNumeric()) {
                    messageCollector.add(ExpectedNumericOperandMessage(leftType, left.position))
                }
                if (!rightType.isNumeric()) {
                    messageCollector.add(ExpectedNumericOperandMessage(rightType, right.position))
                }
                // TODO: more types => more checks
                if (leftType == RealType || rightType == RealType) RealType else leftType
            }

            else -> TODO()
        }
    }

    private fun resolve(compoundStatement: CompoundStatement, scope: Scope) {
        compoundStatement.statements.forEach {
            when (it) {
                is FunctionStatement -> resolve(it.functionCall, scope)
                is Assignment -> resolve(it, scope)
                is ReturnStatement -> resolve(it, scope)
                else -> TODO("stmt=$it")
            }
        }
    }

    private fun Type.isAssignableFrom(type: Type): Boolean {
        if (this == type) return true
        if (this == RealType && type.isNumeric()) return true
        return false
    }

    private fun resolve(returnStatement: ReturnStatement, scope: Scope) {
        returnStatement.expression?.let {
            resolve(it, scope)
            if (scope.returnType == UnitType) {
                messageCollector.add(ProcedureCannotReturnValueMessage(returnStatement.position))
            } else if (it.type != null && !scope.returnType.isAssignableFrom(it.type!!)) {
                messageCollector.add(TypeIsNotAssignableMessage(scope.returnType, it.type!!, returnStatement.position))
            }
        } ?: if (scope.returnType != UnitType) {
            messageCollector.add(ExpectedReturnValueMessage(scope.returnType, returnStatement.position))
        } else {
        }
    }

    private fun resolve(assignment: Assignment, scope: Scope) {
        resolve(assignment.variable, scope)
        resolve(assignment.expression, scope)
        assignment.expression.type?.let { expressionType ->
            assignment.variable.type?.let { variableType ->
                if (!variableType.isAssignableFrom(expressionType)) {
                    messageCollector.add(
                        TypeIsNotAssignableMessage(
                            variableType,
                            expressionType,
                            assignment.position
                        )
                    )
                }
            }
        }
    }

    private fun resolve(functionDeclaration: FunctionDeclaration, scope: Scope) {
        // TODO: support nested functions
        resolve(functionDeclaration.compoundStatement, scope.join(functionDeclaration))
    }

    fun resolve(program: Program) {
        val scope = Scope(
            messageCollector,
            listOf(),
            program.declarations.copy(functions = listOf()),
            externalFunctions,
            UnitType
        )
        program.declarations.functions.forEach {
            resolve(it, scope)
        }
        program.compoundStatement?.let { resolve(it, scope) }
    }
}