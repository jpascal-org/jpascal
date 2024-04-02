package org.jpascal.compiler.frontend.parser.antlr

import org.antlr.v4.kotlinruntime.ast.Position
import org.jpascal.compiler.frontend.ir.*
import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.types.*
import org.jpascal.compiler.frontend.parser.antlr.generated.JPascalBaseVisitor
import org.jpascal.compiler.frontend.parser.antlr.generated.JPascalParser

class JPascalVisitorImpl(private val filename: String) : JPascalBaseVisitor<Any?>() {
    private var program: Program? = null

    fun getProgram() = program

    override fun visitProgram(ctx: JPascalParser.ProgramContext): Any? {
        val declarations = visitProgramBlock(ctx.programBlock())
        val compoundStatement = ctx.programBlock().compoundStatement()?.let {
            visitCompoundStatement(it)
        }
        program = Program(
            packageName = ctx.packagePart()?.fullyQualifiedName()?.text,
            uses = ctx.usesPart().map(::visitUsesPart),
            declarations = declarations,
            compoundStatement = compoundStatement ?: CompoundStatement(listOf()),
            position = mkPosition(ctx.position)
        )
        return program
    }

    override fun visitUsesPart(ctx: JPascalParser.UsesPartContext): Uses =
        ctx.usesAs()?.let {
            Uses(it.fullyQualifiedName().text, it.identifier().text)
        } ?: Uses(ctx.usesSymbols()!!.text, null)

    override fun visitProgramBlock(ctx: JPascalParser.ProgramBlockContext): Declarations {
        val functions = mutableListOf<FunctionDeclaration>()
        val variables = mutableListOf<VariableDeclaration>()
        ctx.procedureAndFunctionDeclarationPart().forEach {
            functions.add(visitProcedureAndFunctionDeclarationPart(it))
        }
        ctx.variableDeclarationPart().forEach {
            variables.addAll(visitVariableDeclarationPart(it))
        }
        return Declarations(functions = functions, variables = variables)
    }

    override fun visitVariableDeclarationPart(ctx: JPascalParser.VariableDeclarationPartContext): List<VariableDeclaration> {
        val variables = mutableListOf<VariableDeclaration>()
        ctx.variableDeclaration().forEach {
            variables.addAll(visitVariableDeclaration(it))
        }
        return variables
    }
    override fun visitVariableDeclaration(ctx: JPascalParser.VariableDeclarationContext): List<VariableDeclaration> {
        return ctx.identifierList().identifier().map {
            VariableDeclaration(it.text, visitType_(ctx.type_()), null)
        }
    }

    override fun visitType_(ctx: JPascalParser.Type_Context): Type {
        ctx.simpleType()?.let {
            return visitSimpleType(it)
        }
        TODO()
    }

    override fun visitSimpleType(ctx: JPascalParser.SimpleTypeContext): Type {
        ctx.typeIdentifier()?.let {
            return it.asType()
        }
        TODO()
    }

    override fun visitProcedureAndFunctionDeclarationPart(ctx: JPascalParser.ProcedureAndFunctionDeclarationPartContext): FunctionDeclaration {
        val decl = ctx.procedureOrFunctionDeclaration()
        return decl.functionDeclaration()?.let {
            visitFunctionDeclaration(it)
        } ?: visitProcedureDeclaration(decl.procedureDeclaration()!!)
    }

    override fun visitProcedureDeclaration(ctx: JPascalParser.ProcedureDeclarationContext): FunctionDeclaration {
        val declarations = visitProcedureAndFunctionBlock(ctx.procedureAndFunctionBlock())
        return FunctionDeclaration(
            identifier = ctx.identifier().text,
            params = ctx.formalParameterList()?.asParameterList() ?: listOf(),
            returnType = UnitType,
            access = visitNullableAccess(ctx.access()),
            declarations = declarations,
            compoundStatement = visitCompoundStatement(ctx.procedureAndFunctionBlock().compoundStatement()),
            position = mkPosition(ctx.position)
        )
    }

    override fun visitProcedureAndFunctionBlock(ctx: JPascalParser.ProcedureAndFunctionBlockContext): Declarations {
        val functions = mutableListOf<FunctionDeclaration>()
        val variables = mutableListOf<VariableDeclaration>()
        ctx.procedureAndFunctionDeclarationPart().forEach {
            functions.add(visitProcedureAndFunctionDeclarationPart(it))
        }
        ctx.variableDeclarationPart().forEach {
            variables.addAll(visitVariableDeclarationPart(it))
        }
        return Declarations(functions = functions, variables = variables)
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

    private fun visitNullableAccess(ctx: JPascalParser.AccessContext?): Access =
        if (ctx != null) {
            if (ctx.PROTECTED() != null) Access.PROTECTED else Access.PRIVATE
        } else Access.PUBLIC

    override fun visitFunctionDeclaration(ctx: JPascalParser.FunctionDeclarationContext): FunctionDeclaration {
        val declarations = visitProcedureAndFunctionBlock(ctx.procedureAndFunctionBlock())
        return FunctionDeclaration(
            identifier = ctx.identifier().text,
            params = ctx.formalParameterList()?.asParameterList() ?: listOf(),
            returnType = ctx.resultType().typeIdentifier().asType(),
            access = visitNullableAccess(ctx.access()),
            declarations = declarations,
            compoundStatement = visitCompoundStatement(ctx.procedureAndFunctionBlock().compoundStatement()),
            position = mkPosition(ctx.position)
        )
    }

    private fun mkPosition(position: Position?): SourcePosition? =
        position?.let { SourcePosition(filename, position.start.toPosition(), position.end.toPosition()) }

    override fun visitCompoundStatement(ctx: JPascalParser.CompoundStatementContext): CompoundStatement =
        CompoundStatement(
            ctx.statements().statement().mapNotNull {
                visitStatement(it)
            },
            position = mkPosition(ctx.position)
        )

    override fun visitStatement(ctx: JPascalParser.StatementContext): Statement? {
        val label = ctx.label()?.let { Label(it.text) }
        ctx.unlabelledStatement().simpleStatement()?.let { stmt ->
            stmt.assignmentStatement()?.let {
                val expression = visitExpression(it.expression())
                val variable = visitSelector(it.selector())
                return Assignment(variable, expression, label, mkPosition(ctx.position))
            }
            stmt.emptyStatement_()?.let { return null }
            stmt.procedureStatement()?.let {
                val call = visitProcedureStatement(it)
                return FunctionStatement(call, label)
            }
            stmt.returnStatement()?.let {
                val expression = it.expression()?.let(::visitExpression)
                return ReturnStatement(expression, label, mkPosition(ctx.position))
            }
            TODO()
        }
        TODO()
    }

    override fun visitProcedureStatement(ctx: JPascalParser.ProcedureStatementContext): FunctionCall {
        val args = ctx.parameterList()?.actualParameter()?.map {
            visitExpression(it.expression())
        } ?: listOf()
        return FunctionCall(ctx.identifier().text, args, mkPosition(ctx.position))
    }

    override fun visitSelector(ctx: JPascalParser.SelectorContext): Variable {
        if (ctx.LBRACK() != null) TODO()
        if (ctx.DOT() != null) TODO()
        return Variable(ctx.identifier().text, mkPosition(ctx.position))
    }

    override fun visitAdditiveoperator(ctx: JPascalParser.AdditiveoperatorContext): Operation {
        if (ctx.PLUS() != null) return ArithmeticOperation.PLUS
        if (ctx.MINUS() != null) return ArithmeticOperation.MINUS
        TODO()
    }

    override fun visitMultiplicativeoperator(ctx: JPascalParser.MultiplicativeoperatorContext): Operation {
        if (ctx.STAR() != null) return ArithmeticOperation.TIMES
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
            return TreeExpression(op, left, right, mkPosition(ctx.position))
        }
        return left
    }

    override fun visitSimpleExpression(ctx: JPascalParser.SimpleExpressionContext): Expression {
        val left = visitTerm(ctx.term())
        ctx.additiveoperator()?.let { operator ->
            val op = visitAdditiveoperator(operator)
            val right = visitSimpleExpression(ctx.simpleExpression()!!)
            return TreeExpression(op, left, right, mkPosition(ctx.position))
        }
        return left
    }

    override fun visitTerm(ctx: JPascalParser.TermContext): Expression {
        val left = visitSignedFactor(ctx.signedFactor())
        ctx.multiplicativeoperator()?.let { operator ->
            val op = visitMultiplicativeoperator(operator)
            val right = visitTerm(ctx.term()!!)
            return TreeExpression(op, left, right, mkPosition(ctx.position))
        }
        return left
    }

    override fun visitSignedFactor(ctx: JPascalParser.SignedFactorContext): Expression {
        ctx.MINUS()?.let {
            return UnaryExpression(ArithmeticOperation.UNARY_MINUS, visitFactor(ctx.factor()), mkPosition(ctx.position))
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
        ctx.functionDesignator()?.let {
            return visitFunctionDesignator(it)
        }
        TODO()
    }

    override fun visitFunctionDesignator(ctx: JPascalParser.FunctionDesignatorContext): FunctionCall =
        FunctionCall(
            ctx.identifier().text,
            ctx.parameterList().actualParameter().map { visitExpression(it.expression()) },
            mkPosition(ctx.position)
        )

    override fun visitUnsignedConstant(ctx: JPascalParser.UnsignedConstantContext): Expression {
        ctx.string()?.let {
            return StringLiteral(it.text.substring(1, it.text.length - 1), mkPosition(ctx.position))
        }
        ctx.unsignedNumber()?.unsignedInteger()?.let {
            return IntegerNumber(it.text.toInt(), mkPosition(ctx.position))
        }
        ctx.unsignedNumber()?.unsignedReal()?.let {
            return RealNumber(it.text.toDouble(), mkPosition(ctx.position))
        }
        TODO()
    }
}