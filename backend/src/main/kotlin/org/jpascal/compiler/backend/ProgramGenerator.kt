package org.jpascal.compiler.backend

import org.jpascal.compiler.common.ir.getJvmClassName
import org.jpascal.compiler.common.ir.getJvmDescriptor
import org.jpascal.compiler.common.ir.globalVariableJvmField
import org.jpascal.compiler.frontend.ir.Access
import org.jpascal.compiler.frontend.ir.Program
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

class ProgramGenerator {
    fun generate(program: Program): CompilationResult {
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        val className = program.getJvmClassName()
        cw.visit(
            Constants.JVM_TARGET_VERSION,
            Opcodes.ACC_PUBLIC,
            className,
            null,
            Type.getInternalName(Object::class.java),
            null
        )
        generateVariables(program, cw)
        generateFunctions(program, cw)
        return CompilationResult(className, cw.toByteArray())
    }

    private fun generateVariables(program: Program, cw: ClassWriter) {
        program.declarations.variables.forEach {
            val field = it.globalVariableJvmField()
            cw.visitField(
                Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
                field.name,
                field.descriptor,
                null,
                null
            )
        }
    }

    private fun generateFunctions(program: Program, cw: ClassWriter) {
        program.declarations.functions.forEach {
            val access = getAccessMask(it.access) or Opcodes.ACC_STATIC
            val mv = cw.visitMethod(
                access,
                it.identifier,
                it.getJvmDescriptor(),
                null,
                null
            )
            FunctionGenerator(LocalVariablesSorter(access, it.getJvmDescriptor(), mv), it).generate()
        }
    }

    private fun getAccessMask(access: Access): Int =
        when (access) {
            Access.PUBLIC -> Opcodes.ACC_PUBLIC
            Access.PROTECTED -> Opcodes.ACC_PROTECTED
            Access.PRIVATE -> Opcodes.ACC_PRIVATE
        }
}