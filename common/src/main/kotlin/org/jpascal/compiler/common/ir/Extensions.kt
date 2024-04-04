package org.jpascal.compiler.common.ir

import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.Program
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.ir.VariableDeclaration
import org.jpascal.compiler.frontend.ir.types.IntegerType
import org.jpascal.compiler.frontend.ir.types.RealType
import org.jpascal.compiler.frontend.ir.types.Type
import org.jpascal.compiler.frontend.ir.types.UnitType
import org.jpascal.compiler.frontend.resolve.JvmField
import java.io.File

fun Program.getJvmClassName() = jvmClassName(this.position!!)

private fun jvmClassName(position: SourcePosition) = File(position.filename).nameWithoutExtension + "Pas"

fun Type.toJvmType(): String =
    when (this) {
        is IntegerType -> "I"
        is RealType -> "D"
        is UnitType -> "V"
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

fun VariableDeclaration.globalVariableJvmField(): JvmField =
    JvmField(jvmClassName(this.position!!), this.name, this.type.toJvmType())