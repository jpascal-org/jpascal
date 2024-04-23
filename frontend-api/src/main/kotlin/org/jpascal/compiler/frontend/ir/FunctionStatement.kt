package org.jpascal.compiler.frontend.ir

data class FunctionStatement(
    val functionCall: FunctionCall,
    override var label: Label? = null,
    override val position: SourcePosition?
) : Statement