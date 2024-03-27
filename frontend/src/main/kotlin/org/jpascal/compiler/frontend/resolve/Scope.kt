package org.jpascal.compiler.frontend.resolve

import org.jpascal.compiler.frontend.ir.Declarations
import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.VariableDeclaration
import org.jpascal.compiler.frontend.ir.types.Type

class Scope(
    declarations: Declarations,
    externalFunctions: Map<String, MutableList<JvmMethod>>,
    val returnType: Type,
    private val parent: Scope? = null
) {
    private val variables = mutableMapOf<String, VariableDeclaration>()
    val functions = mutableMapOf<String, MutableList<JvmMethod>>()

    init {
        declarations.variables.forEach {
            variables[it.name] = it
        }
        functions.putAll(externalFunctions)
        declarations.functions.forEach {
            functions.getOrPut(it.identifier) { mutableListOf() }.add(it.jvmMethod!!)
        }
    }

    fun join(functionDeclaration: FunctionDeclaration): Scope {
        val variables = functionDeclaration
            .params
            .map { VariableDeclaration(it.name, it.type, null) }

        return Scope(
            declarations = Declarations(
                functions = functionDeclaration.declarations.functions,
                variables = functionDeclaration.declarations.variables + variables
            ),
            externalFunctions = mapOf(),
            returnType = functionDeclaration.returnType,
            parent = this
        )
    }

    fun findVariable(name: String): VariableDeclaration? =
        variables[name] ?: parent?.findVariable(name)

    fun findFunctionCandidates(name: String): List<JvmMethod>? = functions[name] ?: parent?.findFunctionCandidates(name)
}