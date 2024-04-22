package org.jpascal.compiler.frontend.ir

data class FunctionStatement(
    val functionCall: FunctionCall,
    override val label: Label? = null,
    override val position: SourcePosition?
) : Statement