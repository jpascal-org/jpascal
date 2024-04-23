package org.jpascal.compiler.frontend.ir

import org.jpascal.compiler.frontend.ir.types.*
import org.jpascal.compiler.frontend.resolve.JvmField
import java.io.File

fun Program.getJvmClassName() = jvmClassName(packageName, this.position!!)

private fun jvmClassName(packageName: String?, position: SourcePosition): String {
    val name = File(position.filename).nameWithoutExtension + "Pas"
    return packageName?.let {
        it.replace('.', '/') + '/' + name
    } ?: name
}

fun Type.toJvmType(): String =
    when (this) {
        is IntegerType -> "I"
        is RealType -> "D"
        is UnitType -> "V"
        is BooleanType -> "Z"
        is StringType -> "Ljava/lang/String;"
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

fun VariableDeclaration.globalVariableJvmField(packageName: String?): JvmField =
    JvmField(jvmClassName(packageName, this.position!!), this.name, this.type.toJvmType())