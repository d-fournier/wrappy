package me.dfournier.wrappy.utils

import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic


/**
 * Handle error reporting for an annotation processor.
 * From Autovalue [snippet](https://github.com/google/auto/blob/1561e2cde6226a54d41d67737e6e6a45dd0cfa63/value/src/main/java/com/google/auto/value/processor/ErrorReporter.java)
 *
 * @author Éamonn McManus
 * @author Donovan Fournier
 */
internal class ErrorReporter(processingEnv: ProcessingEnvironment) {

    private val messager: Messager = processingEnv.messager
    private var anyErrors: Boolean = false

    /**
     * Issue a compilation note.
     *
     * @param msg the text of the note
     * @param e the element to which it pertains
     */
    fun reportNote(msg: String, e: Element) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e)
    }

    /**
     * Issue a compilation warning.
     *
     * @param msg the text of the warning
     * @param e the element to which it pertains
     */
    fun reportWarning(msg: String, e: Element?) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg, e)
    }

    /**
     * Issue a compilation error. This method does not throw an exception, since we want to continue
     * processing and perhaps report other errors. It is a good idea to introduce a test case in
     * CompilationTest for any new call to reportError(...) to ensure that we continue correctly after
     * an error.
     *
     * @param msg the text of the warning
     * @param e the element to which it pertains
     */
    fun reportError(msg: String, e: Element?) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e)
        anyErrors = true
    }

    /**
     * Issue a compilation error and abandon the processing of this class. This does not prevent the
     * processing of other classes.
     *
     * @param msg the text of the error
     * @param e the element to which it pertains
     * @param T Only for compatibility purpose but won't return
     */
    fun <T> abortWithError(msg: String, e: Element?): T {
        reportError(msg, e)
        throw ProcessingException()
    }

    /**
     * Issue a compilation error and abandon the processing of this class. This does not prevent the
     * processing of other classes.
     *
     * @param msg the text of the error
     * @param e the element to which it pertains
     */
    fun abortWithError(msg: String, e: Element?) {
        reportError(msg, e)
        throw ProcessingException()
    }

    /**
     * Abandon the processing of this class if any errors have been output.
     */
    fun abortIfAnyError() {
        if (anyErrors) {
            throw ProcessingException()
        }
    }
}
