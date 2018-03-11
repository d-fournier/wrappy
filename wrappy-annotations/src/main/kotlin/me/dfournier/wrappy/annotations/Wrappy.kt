package me.dfournier.wrappy.annotations

/**
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Wrappy(
        val processor: String
)
