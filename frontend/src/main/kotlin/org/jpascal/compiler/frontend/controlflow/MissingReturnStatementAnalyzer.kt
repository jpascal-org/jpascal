package org.jpascal.compiler.frontend.controlflow

import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.controlflow.messages.MissingReturnStatementMessage
import org.jpascal.compiler.frontend.ir.*

class MissingReturnStatementAnalyzer(private val messageCollector: MessageCollector) {
    fun analyze(functionDeclaration: FunctionDeclaration) {
        if (functionDeclaration.isProcedure()) return
        if (!analyze(functionDeclaration.compoundStatement)) {
            messageCollector.add(MissingReturnStatementMessage(functionDeclaration))
        }
    }

    private fun analyze(statement: CompoundStatement): Boolean {
        statement.statements.reversed().forEach {
            if (analyze(it)) return true
        }
        return false
    }

    private fun analyze(statement: Statement): Boolean =
        when (statement) {
            is ReturnStatement -> true
            is IfStatement -> analyze(statement)
            else -> false
        }

    private fun analyze(statement: IfStatement): Boolean {
        return (statement.elseBranch?.let(::analyze) ?: false) && analyze(statement.thenBranch)
    }
}