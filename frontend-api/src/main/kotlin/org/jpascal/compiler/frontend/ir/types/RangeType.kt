package org.jpascal.compiler.frontend.ir.types

import org.jpascal.compiler.frontend.ir.OrderedValue

data class RangeType<T>(val min: OrderedValue<T>, val max: OrderedValue<T>) : OrderedType