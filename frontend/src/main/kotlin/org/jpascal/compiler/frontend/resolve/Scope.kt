package org.jpascal.compiler.frontend.resolve

import org.jpascal.compiler.frontend.MessageCollector
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.UnitType
import org.jpascal.compiler.frontend.resolve.messages.ElementIsAlreadyDefinedMessage

class Scope(
    private val messageCollector: MessageCollector,
    formalParameters: List<FormalParameter>,
    declarations: Declarations,
    externalFunctions: Map<String, MutableList<JvmMethod>>,
    element: PositionedElement,
    val parent: Scope? = null
) {
    private val variables = mutableMapOf<String, TypedDeclaration>()
    private val functions = mutableMapOf<String, MutableList<JvmMethod>>()
    private val labels = mutableSetOf<String>()
    val packageName: String? = if (element is Program) element.packageName else parent?.packageName
    val returnType = if (element is FunctionDeclaration) element.returnType else UnitType

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
            functionDeclaration,
            parent = this
        )
    }

    fun checkLabel(label: Label): Boolean {
        if (labels.contains(label.name)) return false
        labels.add(label.name)
        return true
    }

    fun findVariableDeclarationScope(name: String): Scope?  =
        if (variables.containsKey(name)) {
            this
        } else {
            parent?.findVariableDeclarationScope(name)
        }

    fun findVariable(name: String): TypedDeclaration? = variables[name]

    fun findFunctionCandidates(name: String): List<JvmMethod>? = functions[name] ?: parent?.findFunctionCandidates(name)
}