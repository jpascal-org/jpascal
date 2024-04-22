package org.jpascal.compiler.backend

import org.jpascal.compiler.frontend.parser.api.Source
import kotlin.test.Test

class PackageTest : BaseBackendTest() {
    @Test
    fun simplePackage() {
        compile(
            "Foo.pas",
            """
            package org.example.foo;
            procedure foo;
            begin
            end;    
            """.trimIndent()
        )
    }

    @Test
    fun multipleFiles() {
        val foo = Source(
            "Foo.pas",
            """
            package org.example;

            procedure foo;
            begin
            end;   
            """.trimIndent(),
        )
        val bar = Source(
            "Bar.pas",
            """
            package org.example;
            
            procedure bar;
            begin
                foo;
            end;
            begin
                bar;
            end.                
            """.trimIndent()
        )
        compile(listOf(foo, bar))
    }
}