package org.jpascal.compiler.frontend.resolve

import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.Type
import org.jpascal.compiler.frontend.resolve.messages.ElementIsAlreadyDefinedMessage

class Scope(
    private val messageCollector: MessageCollector,
    formalParameters: List<FormalParameter>,
    declarations: Declarations,
    externalFunctions: Map<String, MutableList<JvmMethod>>,
    val returnType: Type,
    private val parent: Scope? = null
) {
    private val variables = mutableMapOf<String, TypedDeclaration>()
    private val functions = mutableMapOf<String, MutableList<JvmMethod>>()

    init {
        formalParameters.forEach {
            variables[it.name]?.let { decl ->
                messageCollector.add(ElementIsAlreadyDefinedMessage(it, decl))
            }
            variables[it.name] = it
        }
        declarations.variables.forEach {
            variables[it.name]?.let { decl ->
                messageCollector.add(ElementIsAlreadyDefinedMessage(it, decl))
            }
            variables[it.name] = it
        }
        functions.putAll(externalFunctions)
        declarations.functions.forEach {
            functions.getOrPut(it.identifier) { mutableListOf() }.add(it.jvmMethod!!)
        }
    }

    fun join(functionDeclaration: FunctionDeclaration): Scope {
        return Scope(
            messageCollector,
            functionDeclaration.params,
            Declarations(
                functions = functionDeclaration.declarations.functions,
                variables = functionDeclaration.declarations.variables
            ),
            mapOf(),
            functionDeclaration.returnType,
            parent = this
        )
    }

    fun findVariable(name: String): TypedDeclaration? =
        variables[name] ?: parent?.findVariable(name)

    fun findFunctionCandidates(name: String): List<JvmMethod>? = functions[name] ?: parent?.findFunctionCandidates(name)
}