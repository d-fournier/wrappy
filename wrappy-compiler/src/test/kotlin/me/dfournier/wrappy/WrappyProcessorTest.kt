package me.dfournier.wrappy

import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.nhaarman.mockito_kotlin.*
import me.dfournier.wrappy.model.WrapperDefinition
import me.dfournier.wrappy.utils.*
import org.junit.Test
import javax.lang.model.type.TypeKind


class WrappyProcessorTest {

    @Test
    fun testWithoutExtensions() {
        val compilation = javac().withProcessors(
                WrappyProcessor(emptyList())
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR),
                        getWrappedClass()
                )

        assertThat(compilation).apply {
            failed()
            hadErrorContaining("No generator can be found")
        }
    }

    @Test
    fun testNoExtensionFound() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(OTHER_GENERATOR_1, OTHER_GENERATOR_2)
        }


        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock,
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR),
                        getWrappedClass()
                )

        assertThat(compilation).apply {
            failed()
            hadErrorContaining("The requested generator \"${TEST_GENERATOR}\" cannot be found. " +
                    "Available generators are: [${OTHER_GENERATOR_1}, ${OTHER_GENERATOR_2}]")
        }
        verify(testGeneratorMock, never()).generate(any(), any())
    }

    @Test
    fun testAnnotatedFunctionFormat__tooManyParams() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR, "WrappedClass clazz, WrappedClass clazz2"),
                        getWrappedClass()
                )

        assertThat(compilation).apply {
            failed()
            hadErrorContaining("The annotated method provideWrapper must contain exactly 1 parameter.")
        }
        verify(testGeneratorMock, never()).generate(any(), any())
    }

    @Test
    fun testAnnotatedFunctionFormat__notEnoughParam() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR, ""),
                        getWrappedClass()
                )

        assertThat(compilation).apply {
            failed()
            hadErrorContaining("The annotated method $PROVIDER_METHOD_NAME must contain exactly 1 parameter.")
        }
        verify(testGeneratorMock, never()).generate(any(), any())
    }

    @Test
    fun testAnnotatedFunctionFormat__primitiveParam() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR, "int clazz"),
                        getWrappedClass()
                )

        assertThat(compilation).apply {
            failed()
            hadErrorContaining("The parameter of $PROVIDER_METHOD_NAME should be a complex declared type (Not a primitive)")
        }
        verify(testGeneratorMock, never()).generate(any(), any())
    }

    @Test
    fun testAnnotatedFunctionFormat__existingReturnType() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR),
                        getWrappedClass(),
                        JavaFileObjects.forSourceLines(
                                "foo.bar.TestWrapper",
                                """
                                package foo.bar;

                                public class TestWrapper {
                                }
                                """
                        )

                )

        assertThat(compilation).apply {
            failed()
            hadErrorContaining("The return type of the annotated method $PROVIDER_METHOD_NAME already exist and cannot be generated")
        }
        verify(testGeneratorMock, never()).generate(any(), any())
    }

    @Test
    fun testWrappedClassContent__empty() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR),
                        getWrappedClass()
                )

        assertThat(compilation).apply {
            hadWarningContaining("The wrapped class WrappedClass does not contain eligible methods")
        }
        verify(testGeneratorMock).generate(any(), any())
    }

    @Test
    fun testWrappedClassContent__returnVoid() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR),
                        getWrappedClass(
                                """
                                    public void update() {
                                        // NOOP
                                    }
                                """,
                                """
                                    public Void copy() {
                                        // NOOP
                                    }
                                """
                        )
                )

        assertThat(compilation).apply {
            // No expected error / warning from the processor
        }

        val argumentCaptor = argumentCaptor<WrapperDefinition>()

        verify(testGeneratorMock).generate(any(), argumentCaptor.capture())
        assertThat(argumentCaptor.firstValue.methods.size).isEqualTo(2)
        argumentCaptor.firstValue.methods[0].let {
            assertThat(it.name).isEqualTo("update")
            assertThat(it.returnType.kind).isEqualTo(TypeKind.VOID)
            assertThat(it.params.size).isEqualTo(0)
        }
        argumentCaptor.firstValue.methods[1].let {
            assertThat(it.name).isEqualTo("copy")
            assertThat(it.returnType.kind).isEqualTo(TypeKind.DECLARED)
            assertThat(it.params.size).isEqualTo(0)
        }
    }

    @Test
    fun testWrappedClassContent__returnPrimitive() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR),
                        getWrappedClass(
                                """
                                    public int getA() {
                                        // NOOP
                                    }
                                """,
                                """
                                    public long getB() {
                                        // NOOP
                                    }
                                """
                        )
                )

        assertThat(compilation).apply {
            // No expected error / warning from the processor
        }

        val argumentCaptor = argumentCaptor<WrapperDefinition>()

        verify(testGeneratorMock).generate(any(), argumentCaptor.capture())
        assertThat(argumentCaptor.firstValue.methods.size).isEqualTo(2)
        argumentCaptor.firstValue.methods[0].let {
            assertThat(it.name).isEqualTo("getA")
            assertThat(it.returnType.kind).isEqualTo(TypeKind.INT)
            assertThat(it.params.size).isEqualTo(0)
        }
        argumentCaptor.firstValue.methods[1].let {
            assertThat(it.name).isEqualTo("getB")
            assertThat(it.returnType.kind).isEqualTo(TypeKind.LONG)
            assertThat(it.params.size).isEqualTo(0)
        }
    }

    @Test
    fun testWrappedClassContent__returnDeclaredType() {
        val testGeneratorMock = mock<WrappyGenerator> {
            on { getName() } doReturn listOf(TEST_GENERATOR)
        }

        val compilation = javac().withProcessors(
                WrappyProcessor(listOf(
                        testGeneratorMock
                ))
        )
                .compile(
                        getGenerationClass(TEST_GENERATOR),
                        getWrappedClass(
                                """
                                    public long update(int a, int b) {
                                        // NOOP
                                    }
                                """
                        )
                )

        assertThat(compilation).apply {
            // No expected error / warning from the processor
        }

        val argumentCaptor = argumentCaptor<WrapperDefinition>()

        verify(testGeneratorMock).generate(any(), argumentCaptor.capture())
        assertThat(argumentCaptor.firstValue.methods.size).isEqualTo(1)
        argumentCaptor.firstValue.methods[0].let {
            assertThat(it.name).isEqualTo("update")
            assertThat(it.params.size).isEqualTo(2)
            it.params[0].let {
                assertThat(it.name).isEqualTo("a")
                assertThat(it.type.kind).isEqualTo(TypeKind.INT)
            }
            it.params[1].let {
                assertThat(it.name).isEqualTo("b")
                assertThat(it.type.kind).isEqualTo(TypeKind.INT)
            }
        }
    }
}
