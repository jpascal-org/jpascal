package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.ir.Expression
import org.jpascal.compiler.frontend.ir.FunctionCall
import org.jpascal.compiler.frontend.ir.FunctionStatement
import org.jpascal.compiler.frontend.ir.Program
import org.jpascal.compiler.frontend.resolve.CollectStaticMethodsClassVisitor
import org.jpascal.compiler.frontend.resolve.FunctionIsAlreadyDefinedError
import org.jpascal.compiler.frontend.resolve.JvmMethod
import org.jpascal.compiler.frontend.resolve.UnresolvedFunctionError
import org.objectweb.asm.ClassReader

class Context {
    private val libraryFunctions = mutableMapOf<String, JvmMethod>()

    fun listFunctions(): Map<String, JvmMethod> {
        return libraryFunctions
    }

    fun addSystemLibrary(className: String) {
        val cr = ClassReader(className)
        val cv = CollectStaticMethodsClassVisitor()
        cr.accept(cv, 0)
        cv.listMethods().forEach {
            libraryFunctions[it.name]?.let {
                throw FunctionIsAlreadyDefinedError()
            }
            libraryFunctions[it.name] = it
        }
    }

    private fun resolve(functionCall: FunctionCall) {
        libraryFunctions[functionCall.identifier]?.let {
            functionCall.resolved = it
            functionCall.arguments.forEach { expression ->
                resolve(expression)
            }
            return
        } ?: throw UnresolvedFunctionError(functionCall.identifier)
    }

    private fun resolve(expression: Expression) {

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