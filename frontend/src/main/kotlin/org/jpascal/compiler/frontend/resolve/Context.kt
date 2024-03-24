package org.jpascal.compiler.frontend.resolve

import org.jpascal.compiler.common.Message
import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.common.MessageLevel
import org.jpascal.compiler.common.ir.getJvmClassName
import org.jpascal.compiler.common.ir.getJvmDescriptor
import org.jpascal.compiler.frontend.*
import org.jpascal.compiler.frontend.ir.*
import org.objectweb.asm.ClassReader

class Context(private val messageCollector: MessageCollector) {
    private val resolvedFunctions = mutableMapOf<String, JvmMethod>()

    fun listFunctions(): Map<String, JvmMethod> {
        return resolvedFunctions
    }

    fun addProgram(program: Program) {
        program.declarations?.functions?.forEach { func ->
            val className = program.getJvmClassName()
            val method = JvmMethod(
                owner = program.packageName?.let { "$it.$className" } ?: className,
                name = func.identifier,
                descriptor = func.getJvmDescriptor()
            )
            tryToResolve(func.identifier, method)
        }
    }

    private fun tryToResolve(functionName: String, jvmMethod: JvmMethod) {
        // TODO: overloaded functions are not supported
        resolvedFunctions[functionName]?.let {
            messageCollector.addMessage(
                Message(FrontendMessages.FUNCTION_IS_ALREADY_DEFINED, MessageLevel.ERROR, null, jvmMethod)
            )
        }
        resolvedFunctions[functionName] = jvmMethod
    }

    fun addSystemLibrary(className: String) {
        val cr = ClassReader(className)
        val cv = CollectStaticMethodsClassVisitor()
        cr.accept(cv, 0)
        cv.listMethods().forEach {
            tryToResolve(it.name, it)
        }
    }

    private fun resolve(functionCall: FunctionCall) {
        resolvedFunctions[functionCall.identifier]?.let {
            functionCall.resolved = it
        } ?: messageCollector.addMessage(
            Message(FrontendMessages.CANNOT_RESOLVE_FUNCTION, MessageLevel.ERROR, null, functionCall.identifier)
        )
        functionCall.arguments.forEach(::resolve)
    }

    private fun resolve(expression: Expression) {
        when (expression) {
            is FunctionCall -> resolve(expression)
            is TreeExpression -> {
                resolve(expression.left)
                resolve(expression.right)
            }
        }
    }

    fun resolve(program: Program) {
        program.compoundStatement.statements.forEach {
            when (it) {
                is FunctionStatement -> resolve(it.functionCall)
                else -> TODO()
            }
        }
    }
}