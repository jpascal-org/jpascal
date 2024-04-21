package org.jpascal.compiler

import org.jpascal.compiler.frontend.Message

class ParseError(val messages: List<Message>) : Error()