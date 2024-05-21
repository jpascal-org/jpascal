package org.jpascal.compiler.frontend.ir

sealed interface JumpStatement : Statement {
    val jump: Label?
}