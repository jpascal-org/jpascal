package org.jpascal.compiler

import org.jpascal.compiler.frontend.Message

class ResolveError(val messages: List<Message>) : Error()