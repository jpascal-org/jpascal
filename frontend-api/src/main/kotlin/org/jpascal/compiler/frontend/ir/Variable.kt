package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.Type

data class Variable(val name: String, override var type: Type? = null) : Expression