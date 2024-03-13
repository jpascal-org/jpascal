package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

data class Variable(val name: String, override val type: Type) : Expression