package org.jpascal.compiler.frontend.resolve

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class CollectStaticMethodsClassVisitor : ClassVisitor(Opcodes.ASM9) {
    private val methods = mutableListOf<JvmMethod>()
    private lateinit var className: String

    fun listMethods(): List<JvmMethod> = methods

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        methods.add(
            JvmMethod(
                className,
                name,
                descriptor
            )
        )
        return null
    }
}