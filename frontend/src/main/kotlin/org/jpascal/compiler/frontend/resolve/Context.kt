package org.jpascal.compiler.frontend.resolve

import org.jpascal.compiler.frontend.MessageCollector
import org.jpascal.compiler.frontend.ir.getJvmClassName
import org.jpascal.compiler.frontend.ir.getJvmDescriptor
import org.jpascal.compiler.frontend.ir.globalVariableJvmField
import org.jpascal.compiler.frontend.ir.toJvmType
import org.jpascal.compiler.frontend.controlflow.MissingReturnStatementAnalyzer
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.*
import org.jpascal.compiler.frontend.resolve.messages.*
import org.objectweb.asm.ClassReader

class Context(private val messageCollector: MessageCollector) {
    private val functions = mutableMapOf<String, MutableList<JvmMethod>>()
    private val missingReturnStatementAnalyzer = MissingReturnStatementAnalyzer(messageCollector)

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
            "Z" -> BooleanType
            else -> TODO("type=$returnType")
        }

    fun add(program: Program) {
        // TODO: add uses
        program.declarations.functions.forEach { func ->
            val className = program.getJvmClassName()
            val jvmMethod = JvmMethod(
                owner = className,
                name = func.identifier,
                descriptor = func.getJvmDescriptor()
            )
            func.jvmMethod = jvmMethod
            addFunction(func.identifier, jvmMethod)
        }
    }

    private fun addFunction(functionName: String, jvmMethod: JvmMethod) {
        val overloaded = functions.getOrPut(functionName) { mutableListOf() }
        // TODO: support visibility
        overloaded.find { it.getTypeSignature() == jvmMethod.getTypeSignature() }?.let {
            messageCollector.add(FunctionIsAlreadyDefinedMessage(functionName, it, null))
            return
        }
        overloaded.add(jvmMethod)
    }

    fun addExternalLibrary(className: String) {
        val cr = ClassReader(className)
        val cv = CollectStaticMethodsClassVisitor()
        cr.accept(cv, 0)
        cv.listMethods().forEach {
            addFunction(it.name, it)
        }
    }

    private fun resolve(functionCall: FunctionCall, scope: Scope) {
        functionCall.arguments.forEach { resolve(it, scope) }
        scope.findFunctionCandidates(functionCall.identifier)?.let { candidates ->
            if (candidates.size == 1) {
                if (candidates[0].matchSignature(functionCall)) {
                    functionCall.resolved = candidates[0]
                    functionCall.type = candidates[0].getReturnType()
                } else messageCollector.add(CannotResolveFunctionMessage(functionCall))
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
            if (declarationScope.parent == null) variable.jvmField =
                (decl as VariableDeclaration).globalVariableJvmField(scope.packageName)
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

            is UnaryExpression -> {
                resolve(expression.expression, scope)
                expression.type = expression.expression.type
            }

            is Variable -> resolve(expression, scope)
            else -> { }
        }
//        if (expression.type == null) TODO("expr=$expression")
    }

    private fun Type.isNumeric() =
        when (this) {
            is IntType -> true
            is RealType -> true
            else -> false
        }

    private fun Type.isInt() =
        when (this) {
            is IntType -> true
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
                assertNumericType(leftType, left.position)
                assertNumericType(rightType, right.position)
                // TODO: more types => more checks
                if (leftType == RealType || rightType == RealType) RealType else leftType
            }

            is RelationalOperation -> {
                assertNumericType(leftType, left.position)
                assertNumericType(rightType, right.position)
                BooleanType
            }
            is LogicalOperation -> {
                // to support bitwise operations
                val allowedTypes = listOf(BooleanType, IntegerType)
                assertType(allowedTypes, leftType, left.position)
                assertType(allowedTypes, rightType, right.position)
                if (leftType != rightType) {
                    messageCollector.add(UnmatchedExpressionPartTypes(leftType, rightType, expression.position))
                    null
                } else {
                    leftType
                }
            }
            else -> TODO()
        }
    }

    private fun assertType(expectedType: Type, foundType: Type?, position: SourcePosition?) {
        assertType(listOf(expectedType), foundType, position)
    }

    private fun assertType(expectedType: List<Type>, foundType: Type?, position: SourcePosition?) {
        if (foundType != null && !expectedType.contains(foundType)) {
            messageCollector.add(ExpectedExpressionTypeMessage(expectedType, foundType, position))
        }
    }

    private fun assertNumericType(type: Type?, position: SourcePosition?) {
        if (type != null && !type.isNumeric()) {
            messageCollector.add(ExpectedNumericOperandMessage(type, position))
        }
    }

    private fun assertIntType(type: Type?, position: SourcePosition?) {
        if (type != null && !type.isInt()) {
            messageCollector.add(ExpectedExpressionTypeMessage(INT_TYPES, type, position))
        }
    }

    private fun resolve(compoundStatement: CompoundStatement, scope: Scope) {
        compoundStatement.statements.forEach {
            resolve(it, scope)
        }
    }

    private fun resolve(statement: Statement, scope: Scope) {
        when (statement) {
            is FunctionStatement -> resolve(statement.functionCall, scope)
            is AssignmentStatement -> resolve(statement, scope)
            is ReturnStatement -> resolve(statement, scope)
            is IfStatement -> resolve(statement, scope)
            is CompoundStatement -> resolve(statement, scope)
            is WhileStatement -> resolve(statement, scope)
            is RepeatStatement -> resolve(statement, scope)
            is ForStatement -> resolve(statement, scope)
            is BreakStatement -> resolveBreak(statement, scope)
            else -> TODO("stmt=$statement")
        }
    }

    private fun resolveBreak(statement: BreakStatement, scope: Scope) {
        fun findLoop(statement: Statement, label: Label?): Boolean {
            if ((statement is WhileStatement ||
                statement is RepeatStatement ||
                statement is ForStatement) && label == statement.label) return true

            if (statement.parent != null && statement.parent is Statement)
                return findLoop(statement.parent as Statement, label)

            return false
        }
        if (!findLoop(statement, statement.jumpFrom))
            messageCollector.add(BreakIsOutOfLoopMessage(statement.position))
    }

    private fun resolve(statement: ForStatement, scope: Scope) {
        resolve(statement.variable, scope)
        resolve(statement.initialValue, scope)
        assertIntType(statement.initialValue.type, statement.initialValue.position)
        resolve(statement.finalValue, scope)
        assertIntType(statement.finalValue.type, statement.finalValue.position)
        assertIsAssignableFrom(statement.variable, statement.initialValue)
        assertIsAssignableFrom(statement.variable, statement.finalValue)
        resolve(statement.statement, scope)
    }

    private fun resolve(statement: RepeatStatement, scope: Scope) {
        resolve(statement.condition, scope)
        assertType(BooleanType, statement.condition.type, statement.condition.position)
        resolve(statement.statement ,scope)
    }

    private fun resolve(statement: WhileStatement, scope: Scope) {
        resolve(statement.condition, scope)
        assertType(BooleanType, statement.condition.type, statement.condition.position)
        resolve(statement.statement ,scope)
    }

    private fun resolve(statement: IfStatement, scope: Scope) {
        resolve(statement.condition, scope)
        assertType(BooleanType, statement.condition.type, statement.condition.position)
        resolve(statement.thenBranch, scope)
        statement.elseBranch?.let { resolve(it, scope) }
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
                messageCollector.add(IncompatibleReturnTypeMessage(scope.returnType, it.type!!, returnStatement.position))
            }
        } ?: if (scope.returnType != UnitType) {
            messageCollector.add(ExpectedReturnValueMessage(scope.returnType, returnStatement.position))
        } else {
        }
    }

    private fun resolve(assignmentStatement: AssignmentStatement, scope: Scope) {
        resolve(assignmentStatement.variable, scope)
        resolve(assignmentStatement.expression, scope)
        assertIsAssignableFrom(assignmentStatement.variable, assignmentStatement.expression)
    }

    private fun assertIsAssignableFrom(variable: Variable, expression: Expression) {
        expression.type?.let { expressionType ->
            variable.type?.let { variableType ->
                if (!variableType.isAssignableFrom(expressionType)) {
                    messageCollector.add(VariableTypeIsNotAssignableMessage(variable, expression))
                }
            }
        }
    }

    private fun resolve(functionDeclaration: FunctionDeclaration, scope: Scope) {
        // TODO: support nested functions
        missingReturnStatementAnalyzer.analyze(functionDeclaration)
        resolve(functionDeclaration.compoundStatement, scope.join(functionDeclaration))
    }

    fun resolve(program: Program) {
        val scope = Scope(
            messageCollector,
            listOf(),
            program.declarations.copy(functions = listOf()),
            functions,
            program
        )
        program.declarations.functions.forEach {
            resolve(it, scope)
        }
        program.compoundStatement?.let { resolve(it, scope) }
    }

    companion object {
        val INT_TYPES = listOf(IntegerType)
    }
}