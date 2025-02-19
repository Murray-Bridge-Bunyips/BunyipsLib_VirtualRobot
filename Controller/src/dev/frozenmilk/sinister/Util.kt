package dev.frozenmilk.sinister

import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Predicate
///////
@JvmOverloads
fun Class<*>.getAllMethods(predicate: Predicate<Method> = Predicate { true } ): List<Method> {
	return this.declaredMethods.toList().filter(predicate::test) + (this.superclass?.getAllMethods(predicate) ?: emptyList())
}

@JvmOverloads
fun Class<*>.getAllFields(predicate: Predicate<Field> = Predicate { true } ): List<Field> {
	return this.declaredFields.toList().filter(predicate::test) + (this.superclass?.getAllFields(predicate) ?: emptyList())
}

fun Member.isInterface() : Boolean = Modifier.isInterface(modifiers)
fun Member.isNative() : Boolean = Modifier.isNative(modifiers)
fun Member.isSynchronized() : Boolean = Modifier.isSynchronized(modifiers)
fun Member.isTransient() : Boolean = Modifier.isTransient(modifiers)
fun Member.isVolatile() : Boolean = Modifier.isVolatile(modifiers)
fun Member.isStatic() : Boolean = Modifier.isStatic(modifiers)
fun Member.isPublic() : Boolean = Modifier.isPublic(modifiers)
fun Member.isPrivate() : Boolean = Modifier.isPrivate(modifiers)
fun Member.isProtected() : Boolean = Modifier.isProtected(modifiers)
fun Member.isAbstract() : Boolean = Modifier.isAbstract(modifiers)
fun Member.isFinal() : Boolean = Modifier.isFinal(modifiers)

//
fun <T> Class<*>.staticInstancesOf(type: Class<T>) : List<T> =
	this.getAllFields {
		it.isStatic() && type.isAssignableFrom(it.type)
	}
	.mapNotNull {
		it.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		it.get(this) as? T
	}

@Throws(NoLoaderException::class)
fun Class<*>.load() = classLoader?.loadClass(this.name) ?: throw NoLoaderException()

@Throws(NoLoaderException::class, NoPreloadException::class)
fun Class<*>.preload() = if (!inheritsAnnotation(Preload::class.java)) throw NoPreloadException() else load()

class NoLoaderException : Exception("Tried to load a class with no class loader")

class NoPreloadException : Exception("Tried to load a class that isn't allowed to be preloaded")

fun Class<*>.inheritsAnnotation(annotation: Class<out Annotation>): Boolean = if (isAnnotationPresent(annotation)) true else interfaces.any { it.inheritsAnnotation(annotation) } || (superclass?.inheritsAnnotation(annotation) ?: false)

@JvmOverloads
fun <A: Annotation> Class<*>.getAllAnnotationsByType(cls: Class<A>, predicate: Predicate<A> = Predicate { true } ): List<A> {
	val res = this.getAnnotationsByType(cls).filter(predicate::test) + this.interfaces.flatMap { it.getAllAnnotationsByType(cls, predicate) }
	val superRes = this.superclass?.getAllAnnotationsByType(cls, predicate)
	return if (superRes != null) res + superRes
	else res
}

@JvmOverloads
fun Class<*>.getAllAnnotations(predicate: Predicate<Annotation> = Predicate { true } ): List<Annotation> {
	val res = this.declaredAnnotations.filter(predicate::test) + this.interfaces.flatMap { it.getAllAnnotations(predicate) }
	val superRes = this.superclass?.getAllAnnotations(predicate)
	return if (superRes != null) res + superRes
	else res
}