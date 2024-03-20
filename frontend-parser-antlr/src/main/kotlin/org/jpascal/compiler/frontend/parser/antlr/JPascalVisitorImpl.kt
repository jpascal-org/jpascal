package org.jpascal.compiler.frontend.parser.antlr

import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.types.*
import org.jpascal.compiler.frontend.parser.antlr.generated.JPascalBaseVisitor
import org.jpascal.compiler.frontend.parser.antlr.generated.JPascalParser

class JPascalVisitorImpl(private val filename: String) : JPascalBaseVisitor<Any?>() {
    private var program: Program? = null

    data class ProgramBlock(
        val functions: List<FunctionDeclaration>
    )

    fun getProgram() = program

    override fun visitProgram(ctx: JPascalParser.ProgramContext): Any? {
        val programBlock = visitProgramBlock(ctx.programBlock())
        val compoundStatement = ctx.programBlock().compoundStatement()?.let {
            visitCompoundStatement(it)
        }
        program = Program(
            name = ctx.programHeading()?.identifier()?.text,
            uses = null,
            declarations = Declarations(functions = programBlock.functions, variables = listOf()),
            compoundStatement = compoundStatement ?: CompoundStatement(listOf()),
            position = ctx.position!!.let {
                SourcePosition(filename, it.start.toPosition(), it.end.toPosition())
            }
        )
        return program
    }

    override fun visitProgramBlock(ctx: JPascalParser.ProgramBlockContext): ProgramBlock {
        val functions = mutableListOf<FunctionDeclaration>()
        ctx.procedureAndFunctionDeclarationPart().forEach {
            functions.add(visitProcedureAndFunctionDeclarationPart(it))
        }
        return ProgramBlock(functions = functions)
    }

    override fun visitProcedureAndFunctionDeclarationPart(ctx: JPascalParser.ProcedureAndFunctionDeclarationPartContext): FunctionDeclaration {
        ctx.procedureOrFunctionDeclaration().functionDeclaration()?.let {
            return visitFunctionDeclaration(it)
        }
        TODO()
    }

    private fun JPascalParser.TypeIdentifierContext.asType(): Type {
        if (CHAR() != null) return CharType
        if (INTEGER() != null) return IntegerType
        if (REAL() != null) return RealType
        if (BOOLEAN() != null) return BooleanType
        return identifier()?.let { RawType(it.text) } ?: TODO()
    }

    private fun JPascalParser.FormalParameterListContext.asParameterList(): List<FormalParameter> {
        val result = mutableListOf<FormalParameter>()
        val sections = formalParameterSection()
        sections.forEach {
            val group = it.parameterGroup()
            group.identifierList().identifier().forEach { identifier ->
                result.add(
                    FormalParameter(
                        name = identifier.text,
                        type = group.typeIdentifier().asType(),
                        pass = if (it.VAR() != null) Pass.VAR else Pass.VALUE
                    )
                )
            }
        }
        return result
    }

    override fun visitFunctionDeclaration(ctx: JPascalParser.FunctionDeclarationContext) =
        FunctionDeclaration(
            identifier = ctx.identifier().text,
            params = ctx.formalParameterList()?.asParameterList() ?: listOf(),
            returnType = ctx.resultType().typeIdentifier().asType(),
            declarations = null,
            compoundStatement = visitCompoundStatement(ctx.procedureAndFunctionBlock().compoundStatement()),
            position = ctx.position!!.let {
                SourcePosition(filename, it.start.toPosition(), it.end.toPosition())
            }
        )

    override fun visitCompoundStatement(ctx: JPascalParser.CompoundStatementContext): CompoundStatement =
        CompoundStatement(
            ctx.statements().statement().mapNotNull {
                visitStatement(it)
            }
        )

    override fun visitStatement(ctx: JPascalParser.StatementContext): Statement? {
        val label = ctx.label()?.let { Label(it.text) }
        ctx.unlabelledStatement().simpleStatement()?.let { stmt ->
            stmt.assignmentStatement()?.let {
                val expression = visitExpression(it.expression())
                val variable = visitSelector(it.selector())
                return Assignment(
                    variable = variable,
                    expression = expression,
                    label = label
                )
            }
            stmt.emptyStatement_()?.let { return null }
            stmt.procedureStatement()?.let {
                val call = visitProcedureStatement(it)
                return FunctionStatement(call, label)
            }
            TODO()
        }
        TODO()
    }

    override fun visitProcedureStatement(ctx: JPascalParser.ProcedureStatementContext): FunctionCall {
        val args = ctx.parameterList()?.actualParameter()?.map {
            visitExpression(it.expression())
        } ?: listOf()
        return FunctionCall(ctx.identifier().text, args)
    }

    override fun visitSelector(ctx: JPascalParser.SelectorContext): Variable {
        if (ctx.LBRACK() != null) TODO()
        if (ctx.DOT() != null) TODO()
        return Variable(ctx.identifier().text)
    }

    override fun visitAdditiveoperator(ctx: JPascalParser.AdditiveoperatorContext): Operation {
        if (ctx.PLUS() != null) return ArithmeticOperation.PLUS
        if (ctx.MINUS() != null) return ArithmeticOperation.MINUS
        TODO()
    }

    override fun visitMultiplicativeoperator(ctx: JPascalParser.MultiplicativeoperatorContext): Operation {
        TODO()
    }

    override fun visitRelationaloperator(ctx: JPascalParser.RelationaloperatorContext): Operation {
        TODO()
    }

    override fun visitExpression(ctx: JPascalParser.ExpressionContext): Expression {
        val left = visitSimpleExpression(ctx.simpleExpression())
        ctx.relationaloperator()?.let { operator ->
            val op = visitRelationaloperator(operator)
            val right = visitExpression(ctx.expression()!!)
            return TreeExpression(op, left, right)
        }
        return left
    }

    override fun visitSimpleExpression(ctx: JPascalParser.SimpleExpressionContext): Expression {
        val left = visitTerm(ctx.term())
        ctx.additiveoperator()?.let { operator ->
            val op = visitAdditiveoperator(operator)
            val right = visitSimpleExpression(ctx.simpleExpression()!!)
            return TreeExpression(op, left, right)
        }
        return left
    }

    override fun visitTerm(ctx: JPascalParser.TermContext): Expression {
        val left = visitSignedFactor(ctx.signedFactor())
        ctx.multiplicativeoperator()?.let { operator ->
            val op = visitMultiplicativeoperator(operator)
            val right = visitTerm(ctx.term()!!)
            return TreeExpression(op, left, right)
        }
        return left
    }

    override fun visitSignedFactor(ctx: JPascalParser.SignedFactorContext): Expression {
        ctx.MINUS()?.let {
            return UnaryExpression(ArithmeticOperation.UNARY_MINUS, visitFactor(ctx.factor()))
        }
        return visitFactor(ctx.factor())
    }

    override fun visitFactor(ctx: JPascalParser.FactorContext): Expression {
        ctx.selector()?.let {
            return visitSelector(it)
        }
        ctx.unsignedConstant()?.let {
            return visitUnsignedConstant(it)
        }
        TODO()
    }

    override fun visitUnsignedConstant(ctx: JPascalParser.UnsignedConstantContext): Expression {
        ctx.string()?.let {
            return StringLiteral(it.text.substring(1, it.text.length - 1))
        }
        TODO()
    }
}