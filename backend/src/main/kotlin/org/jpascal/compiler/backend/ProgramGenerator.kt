package org.jpascal.compiler.backend

import org.jpascal.compiler.frontend.ir.Program
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class ProgramGenerator(private val context: Context) {
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
        program.declarations?.functions?.forEach {
            val mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
                it.name,
                it.getJvmDescriptor(),
                null,
                null
            )
            FunctionGenerator(context, mv, it).generate()
        }
        // FIXME: generate main
        return CompilationResult(className, cw.toByteArray())
    }
}