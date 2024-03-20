package org.jpascal.compiler.backend

import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.Program
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.ir.types.Type
import java.io.File

fun Program.getJvmClassName() = File(this.position.filename).nameWithoutExtension

fun Type.toJvmType(): String =
    when (this) {
        is IntegerType -> "I"
        else -> TODO(this.toString())
    }

fun FunctionDeclaration.getJvmDescriptor(): String {
    val sb = StringBuilder()
    sb.append("(")
    params.map {
        sb.append(it.type.toJvmType())
    }
    sb.append(")")
    sb.append(returnType.toJvmType())
    return sb.toString()
}