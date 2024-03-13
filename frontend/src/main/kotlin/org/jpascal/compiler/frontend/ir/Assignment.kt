package org.jpascal.compiler.frontend.ir

data class Assignment(val variable: Variable, val expression: Expression) : Operator