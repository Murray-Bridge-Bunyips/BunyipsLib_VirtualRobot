@file:Suppress("unused")
@file:JvmName("ContextRules")
package dev.frozenmilk.util.graph.rule

import java.lang.Class
import java.lang.reflect.AnnotatedElement

/**
 * no-op
 */
fun <CTX: Any> contextLess() = NonCapturingContextRule<CTX> {}

/**
 * if [CTX] has annotation with a class of type [cls]
 */
fun <ANNOTATION: Annotation, CTX: AnnotatedElement> annotatedWith(cls: Class<ANNOTATION>) = CapturingContextRule<ANNOTATION, CTX> { context ->
	context.getAnnotation(cls)
}
