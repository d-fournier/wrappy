package me.dfournier.wrappy

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import me.dfournier.wrappy.utils.getGenerationClass
import me.dfournier.wrappy.utils.getWrappedClass
import org.junit.Test

class RxJava2GeneratorTest {

    @Test
    fun testRxJava2Generator() {
        val compilation = Compiler.javac().withProcessors(
                WrappyProcessor(listOf(
                        RxJava2Generator()
                ))
        )
                .compile(
                        getGenerationClass(RxJava2Generator.NAME),
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
    fun testSimpleWrappedClass__simpleMethod() {
        val compilation = Compiler.javac().withProcessors(
                WrappyProcessor(listOf(
                        RxJava2Generator()
                ))
        )
                .compile(
                        getGenerationClass(RxJava2Generator.NAME),
                        getWrappedClass(
                                """
                                    public void update() {
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

                                import io.reactivex.Completable;
                                import io.reactivex.functions.Action;
                                import java.lang.Override;

                                public final class TestWrapper {
                                  private WrappedClass wrappedClass;

                                  public TestWrapper(WrappedClass wrappedClass) {
                                    this.wrappedClass = wrappedClass;
                                  }

                                  public Completable update() {
                                    return Completable.fromAction(new Action() {
                                      @Override
                                      public void run() {
                                        wrappedClass.update();
                                      }
                                    });
                                  }
                                }

                            """.trimIndent()
                    )
        }
    }

    @Test
    fun testSimpleWrappedClass__primitiveGetter() {
        val compilation = Compiler.javac().withProcessors(
                WrappyProcessor(listOf(
                        RxJava2Generator()
                ))
        )
                .compile(
                        getGenerationClass(RxJava2Generator.NAME),
                        getWrappedClass(
                                """
                                    public int getInt() {
                                      return 42;
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

                                import io.reactivex.Single;
                                import java.lang.Integer;
                                import java.lang.Override;
                                import java.util.concurrent.Callable;

                                public final class TestWrapper {
                                  private WrappedClass wrappedClass;

                                  public TestWrapper(WrappedClass wrappedClass) {
                                    this.wrappedClass = wrappedClass;
                                  }

                                  public Single<Integer> getInt() {
                                    return Single.fromCallable(new Callable<Integer>() {
                                      @Override
                                      public Integer call() {
                                        return wrappedClass.getInt();
                                      }
                                    });
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
                        RxJava2Generator()
                ))
        )
                .compile(
                        getGenerationClass(RxJava2Generator.NAME),
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

                                import io.reactivex.Single;
                                import java.lang.IllegalStateException;
                                import java.lang.Override;
                                import java.lang.String;
                                import java.util.concurrent.Callable;

                                public final class TestWrapper {
                                  private WrappedClass wrappedClass;

                                  public TestWrapper(WrappedClass wrappedClass) {
                                    this.wrappedClass = wrappedClass;
                                  }

                                  public Single<String> getString() {
                                    return Single.fromCallable(new Callable<String>() {
                                      @Override
                                      public String call() {
                                        String value = wrappedClass.getString();
                                        if (value != null) {
                                          return value;
                                        }
                                        throw new IllegalStateException();
                                      }
                                    });
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
                        RxJava2Generator()
                ))
        )
                .compile(
                        getGenerationClass(RxJava2Generator.NAME),
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

                                import io.reactivex.Completable;
                                import io.reactivex.functions.Action;
                                import java.lang.Override;
                                import java.lang.String;

                                public final class TestWrapper {
                                  private WrappedClass wrappedClass;

                                  public TestWrapper(WrappedClass wrappedClass) {
                                    this.wrappedClass = wrappedClass;
                                  }

                                  public Completable compute(final String a, final int b) {
                                    return Completable.fromAction(new Action() {
                                      @Override
                                      public void run() {
                                        wrappedClass.compute(a, b);
                                      }
                                    });
                                  }
                                }

                            """.trimIndent()
                    )
        }
    }


}