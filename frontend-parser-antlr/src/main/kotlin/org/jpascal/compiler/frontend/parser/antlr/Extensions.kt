package org.jpascal.compiler.frontend.parser.antlr

import org.antlr.v4.kotlinruntime.ast.Point
import org.jpascal.compiler.frontend.ir.Position

fun Point.toPosition() = Position(line, column + 1)