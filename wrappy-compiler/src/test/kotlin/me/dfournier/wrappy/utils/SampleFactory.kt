package me.dfournier.wrappy.utils

import com.google.testing.compile.JavaFileObjects
import javax.tools.JavaFileObject

fun getGenerationClass(processorName: String, parameter: String = "WrappedClass clazz"): JavaFileObject =
        JavaFileObjects.forSourceLines(
                "foo.bar.A",
                """
                package foo.bar;

                import me.dfournier.wrappy.annotations.Wrappy;

                public class A {
                    @Wrappy(processor="$processorName")
                    public TestWrapper $PROVIDER_METHOD_NAME($parameter) {
                        return new TestWrapper(clazz);
                    }
                }
                """
        )


fun getWrappedClass(vararg content: String): JavaFileObject {

    val builder = StringBuilder()

    content.forEach {
        builder.appendln(it)
    }

    return JavaFileObjects.forSourceLines(
            "foo.bar.WrappedClass",
            """
                package foo.bar;

                public class WrappedClass {
                        $builder
                }
                """
    )
}

