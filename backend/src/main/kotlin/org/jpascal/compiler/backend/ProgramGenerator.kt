package org.jpascal.compiler.backend

import org.jpascal.compiler.common.ir.getJvmClassName
import org.jpascal.compiler.common.ir.getJvmDescriptor
import org.jpascal.compiler.common.ir.globalVariableJvmField
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.types.UnitType
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

class ProgramGenerator(private val program: Program) {
    private val cw = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)

    fun generate(): CompilationResult {
        val className = program.getJvmClassName()
        cw.visit(
            Constants.JVM_TARGET_VERSION,
            Opcodes.ACC_PUBLIC,
            className,
            null,
            Type.getInternalName(Object::class.java),
            null
        )
        program.declarations.variables.forEach(::generateGlobalVariable)
        program.declarations.functions.forEach(::generateFunction)
        program.compoundStatement?.let(::generateMain)
        return CompilationResult(className, cw.toByteArray())
    }

    private fun generateGlobalVariable(variable: VariableDeclaration) {
        val field = variable.globalVariableJvmField()
        cw.visitField(
            Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC,
            field.name,
            field.descriptor,
            null,
            null
        )
    }

    private fun generateFunction(function: FunctionDeclaration, desc: String = function.getJvmDescriptor()) {
        val access = getAccessMask(function.access) or Opcodes.ACC_STATIC
        val mv = cw.visitMethod(
            access,
            function.identifier,
            desc,
            null,
            null
        )
        FunctionGenerator(LocalVariablesSorter(access, desc, mv), function).generate()
    }

    private fun generateMain(compoundStatement: CompoundStatement) {
        val main = FunctionDeclaration(
            identifier = "main",
            params = listOf(),
            returnType = UnitType,
            access = Access.PUBLIC,
            declarations = Declarations(),
            compoundStatement = compoundStatement,
            position = compoundStatement.position
        )
        generateFunction(main, "([Ljava/lang/String;)V")
    }

    private fun getAccessMask(access: Access): Int =
        when (access) {
            Access.PUBLIC -> Opcodes.ACC_PUBLIC
            Access.PROTECTED -> Opcodes.ACC_PROTECTED
            Access.PRIVATE -> Opcodes.ACC_PRIVATE
        }
}