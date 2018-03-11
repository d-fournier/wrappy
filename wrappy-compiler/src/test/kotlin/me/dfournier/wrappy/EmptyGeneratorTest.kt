package me.dfournier.wrappy

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import me.dfournier.wrappy.generator.EmptyClassGenerator
import me.dfournier.wrappy.utils.getGenerationClass
import me.dfournier.wrappy.utils.getWrappedClass
import org.junit.Test

class EmptyGeneratorTest {

    @Test
    fun testEmptyClassGenerator() {
        val compilation = Compiler.javac().withProcessors(
                WrappyProcessor(listOf(
                        EmptyClassGenerator()
                ))
        )
                .compile(
                        getGenerationClass(EmptyClassGenerator.NAME),
                        getWrappedClass()
                )

        CompilationSubject.assertThat(compilation).apply {
            val file = generatedSourceFile("foo.bar.TestWrapper")
            file.contentsAsUtf8String()
                    .isEqualTo(
                            """
                                // Generated File, do not modify
                                package foo.bar;

                                public final class TestWrapper {
                                  private WrappedClass wrappedClass;

                                  public TestWrapper(WrappedClass wrappedClass) {
                                    this.wrappedClass = wrappedClass;
                                  }
                                }

                                """.trimIndent()
                    )
        }
    }

    @Test
    fun testSimpleWrappedClass__stringGetter() {
        val compilation = Compiler.javac().withProcessors(
                WrappyProcessor(listOf(
                        EmptyClassGenerator()
                ))
        )
                .compile(
                        getGenerationClass(EmptyClassGenerator.NAME),
                        getWrappedClass(
                                """
                                    public String getString() {
                                        return "String";
                                    }
                                """.trimIndent()
                        )
                )

        CompilationSubject.assertThat(compilation).apply {
            val file = generatedSourceFile("foo.bar.TestWrapper")
            file.contentsAsUtf8String()
                    .isEqualTo(
                            """
                                // Generated File, do not modify
                                package foo.bar;

                                import java.lang.String;

                                public final class TestWrapper {
                                  private WrappedClass wrappedClass;

                                  public TestWrapper(WrappedClass wrappedClass) {
                                    this.wrappedClass = wrappedClass;
                                  }

                                  public String getString() {
                                    return wrappedClass.getString();
                                  }
                                }

                            """.trimIndent()
                    )
        }
    }

    @Test
    fun testSimpleWrappedClass__params() {
        val compilation = Compiler.javac().withProcessors(
                WrappyProcessor(listOf(
                        EmptyClassGenerator()
                ))
        )
                .compile(
                        getGenerationClass(EmptyClassGenerator.NAME),
                        getWrappedClass(
                                """
                                    public void compute(String a, int b) {
                                        // NOOP
                                    }
                                """.trimIndent()
                        )
                )

        CompilationSubject.assertThat(compilation).apply {
            val file = generatedSourceFile("foo.bar.TestWrapper")
            file.contentsAsUtf8String()
                    .isEqualTo(
                            """
                                // Generated File, do not modify
                                package foo.bar;

                                import java.lang.String;

                                public final class TestWrapper {
                                  private WrappedClass wrappedClass;

                                  public TestWrapper(WrappedClass wrappedClass) {
                                    this.wrappedClass = wrappedClass;
                                  }

                                  public void compute(final String a, final int b) {
                                    wrappedClass.compute(a, b);
                                  }
                                }

                            """.trimIndent()
                    )
        }
    }


}