package org.jpascal.compiler.frontend.ir

data class AssignmentStatement(
    val variable: Variable,
    val expression: Expression,
    override var label: Label? = null,
    override val position: SourcePosition? = null
) : Statement