package me.dfournier.wrappy

import com.google.auto.service.AutoService
import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.ImmutableList
import me.dfournier.wrappy.annotations.Wrappy
import me.dfournier.wrappy.model.AnnotationProcessingContext
import me.dfournier.wrappy.model.MethodDefinition
import me.dfournier.wrappy.model.ParameterDefinition
import me.dfournier.wrappy.model.WrapperDefinition
import me.dfournier.wrappy.utils.ErrorReporter
import me.dfournier.wrappy.utils.ProcessingException
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ErrorType
import javax.lang.model.type.TypeKind


@AutoService(Processor::class)
class WrappyProcessor(private val classLoader: ClassLoader?) : AbstractProcessor() {

    private var extensions: List<WrappyGenerator>? = null
    private lateinit var extensionsByName: Map<String, WrappyGenerator>
    private lateinit var errorReporter: ErrorReporter


    constructor() : this(WrappyProcessor::class.java.classLoader)

    @VisibleForTesting
    constructor(testExtensions: Iterable<WrappyGenerator>) : this(null) {
        this.extensions = testExtensions.toList()
    }


    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)

        errorReporter = ErrorReporter(processingEnv)

        if (extensions == null) {
            extensions = retrieveGenerator()
        }
        extensionsByName = extensions?.map { it.getName() to it }?.toMap() ?: emptyMap()
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(
                Wrappy::class.java.canonicalName
        )
    }


    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        try {
            processEnvironment(roundEnv)
        } catch (e: ProcessingException) {

        }
        return true
    }

    private fun processEnvironment(roundEnv: RoundEnvironment) {
        val wrapperMethodToGenerate = roundEnv.getElementsAnnotatedWith(Wrappy::class.java)
                .filterIsInstance(ExecutableElement::class.java)

        wrapperMethodToGenerate.forEach {
            val annotation = it.getAnnotation(Wrappy::class.java)

            if (extensionsByName.isEmpty()) {
                errorReporter.abortWithError("No generator can be found", null)
            }

            val generatorExtension = extensionsByName[annotation.processor]
                    ?: errorReporter.abortWithError<WrappyGenerator>(
                            "The requested generator \"${annotation.processor}\" cannot be found. " +
                                    "Available generators are: ${extensionsByName.keys}",
                            it)


            val methodParams = it.parameters

            if (methodParams.size != 1) {
                errorReporter.abortWithError("The annotated method ${it.simpleName} must contain exactly 1 parameter.", it)
            }
            val param = methodParams[0]

            val paramType = param.asType()
            if (paramType.kind != TypeKind.DECLARED) {
                errorReporter.abortWithError("The parameter of ${it.simpleName} should be a complex declared type (Not a primitive)", it)
            }

            // Error because the class doesn't exist yet and cannot be accessed during annotation processing
            if (it.returnType.kind != TypeKind.ERROR) {
                errorReporter.abortWithError("The return type of the annotated method ${it.simpleName} already exist and cannot be generated", it)
            }

            val returnType = it.returnType as ErrorType
            val returnPackageName = processingEnv.elementUtils.getPackageOf(it.enclosingElement).qualifiedName.toString()

            val paramElement = (paramType as DeclaredType).asElement()
            val methodsDefinitions = paramElement.enclosedElements
                    .filter { it.asType().kind == TypeKind.EXECUTABLE }
                    .filterIsInstance(ExecutableElement::class.java)    // Only keep methods
                    .filter {
                        it.modifiers.contains(Modifier.PUBLIC)
                                && !it.modifiers.contains(Modifier.STATIC)
                    }
                    .filter { !it.simpleName.contentEquals("<init>") }  // Exclude constructor
                    .also {
                        if (it.isEmpty()) {
                            errorReporter.reportWarning("The wrapped class ${paramElement.simpleName} does not contain eligible methods", paramElement)
                        }
                    }
                    .map {
                        val name = it.simpleName

                        val params = it.parameters
                                .map {
                                    ParameterDefinition(
                                            it.simpleName.toString(),
                                            it.asType()
                                    )
                                }

                        MethodDefinition(
                                name.toString(),
                                it.returnType,
                                params
                        )
                    }
            generatorExtension.generate(
                    AnnotationProcessingContext(processingEnv),
                    WrapperDefinition(returnPackageName, returnType.toString(), paramType, methodsDefinitions)
            )
        }
    }

    // Retrieve all implementation of Wrappy Generator
    /**
     * From AutoValue [snippet](https://github.com/google/auto/blob/1561e2cde6226a54d41d67737e6e6a45dd0cfa63/value/src/main/java/com/google/auto/value/processor/AutoValueProcessor.java)
     */
    private fun retrieveGenerator(): List<WrappyGenerator> {
        return try {
            ServiceLoader.load(WrappyGenerator::class.java, classLoader).toList()
            // ServiceLoader.load returns a lazily-evaluated Iterable, so evaluate it eagerly now
            // to discover any exceptions.
        } catch (t: Throwable) {
            val warning = StringBuilder("An exception occurred while looking for AutoValue extensions. No extensions will function.")
            if (t is ServiceConfigurationError) {
                warning.append(" This may be due to a corrupt jar file in the compiler's classpath.")
            }
            warning.append(" Exception: ").append(t)
            errorReporter.abortWithError(warning.toString(), null)
            ImmutableList.of()
        }
    }
}
