package org.jpascal.compiler

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.vararg
import org.jpascal.compiler.backend.*
import org.jpascal.compiler.frontend.MessageCollector
import org.jpascal.compiler.frontend.parser.antlr.AntlrParserFacadeImpl
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.Context
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier

object JPascal {
    private fun compileFiles(files: List<String>): List<CompilationResult> {
        val messageCollector = MessageCollector()
        val parsed = files.map {
            val parser = AntlrParserFacadeImpl()
            val code = File(it).readText()
            parser.parse(Source(it, code), messageCollector)
        }
        if (messageCollector.list().isNotEmpty()) throw ParseError(messageCollector.list())
        val context = Context(messageCollector)
        context.addExternalLibrary("org.jpascal.stdlib.PreludeKt")
        parsed.forEach(context::add)
        parsed.forEach(context::resolve)
        if (messageCollector.list().isNotEmpty()) throw ResolveError(messageCollector.list())
        return parsed.map {
            val generator = ProgramGenerator(it)
            generator.generate()
        }
    }

    private fun List<CompilationResult>.write(outputDirectory: String?) {
        forEach { result -> result.write(outputDirectory) }
    }

    private fun runFiles(files: List<String>, entryPoint: String?) {
        fun Map<String, Class<*>>.findEntryPoint(): List<Pair<String, Method>> =
            toList().mapNotNull { (className, clazz) ->
                try {
                    clazz.getMethod("main", Array<String>::class.java)
                } catch (e: NoSuchMethodException) {
                    null
                }?.let { method ->
                    if (method.modifiers and Modifier.PUBLIC > 0 &&
                        method.modifiers and Modifier.STATIC > 0
                    ) className to method else null
                }
            }

        val classes = compileFiles(files).toMap().toClasses()
        val entryPoints = classes.findEntryPoint()
        val method = entryPoint?.let {
            entryPoints.toMap()[it] ?: throw CantMatchEntryPointError(entryPoint)
        } ?: if (entryPoints.size == 1) entryPoints[0].second else
            throw MultipleEntryPointsError(entryPoints.map { it.first })
        method.invoke(null, arrayOf<String>())
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("jpascal")
        val run by parser
            .option(ArgType.Boolean, "run", "r", "Run")
            .default(false)
        val entryPoint by parser
            .option(ArgType.String, "main", "m", "Specify entry point")
        val outputDirectory by parser
            .option(ArgType.String, "output", "o", "Specify output directory")
        val files by parser
            .argument(ArgType.String, "files", "Files to compile")
            .vararg()

        parser.parse(args)
        try {
            if (run) runFiles(files, entryPoint) else compileFiles(files).write(outputDirectory)
        } catch (e: ResolveError) {
            e.messages.forEach { println(it) }
        }
    }
}