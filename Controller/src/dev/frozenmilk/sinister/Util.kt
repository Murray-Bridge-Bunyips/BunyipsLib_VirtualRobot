@file:JvmName("SinisterUtil")
package dev.frozenmilk.sinister

import dev.frozenmilk.sinister.loading.Preload
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.function.Predicate
import java.util.zip.ZipFile
import kotlin.math.min

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
fun Member.isPublic() : Boolean = Modifier.isPublic(modifiers)////
fun Member.isPrivate() : Boolean = Modifier.isPrivate(modifiers)
fun Member.isProtected() : Boolean = Modifier.isProtected(modifiers)
fun Member.isAbstract() : Boolean = Modifier.isAbstract(modifiers)
fun Member.isFinal() : Boolean = Modifier.isFinal(modifiers)

/**
 * used to find all [Scanner]s, will give access to all loaded instances of a preloaded class, which is useful for kotlin objects, and similar java singletons
 */
fun <T : Any> Class<*>.staticInstancesOf(type: Class<T>) : Set<T> =////////////
	this.getAllFields {
		it.isStatic() && type.isAssignableFrom(it.type)
	}
	.mapNotNullTo(mutableSetOf()) {
		it.isAccessible = true
		@Suppress("UNCHECKED_CAST")
		it.get(this) as? T
	}

@Suppress("CheckResult")
@Throws(NoLoaderException::class)
fun Class<*>.stageLoad(classLoader: ClassLoader?) = classLoader?.loadClass(this.name).apply {
	getAllFields {
		it.isStatic()
	}.forEach {
		it.isAccessible = true
		it.get(this)
	}
} ?: throw NoLoaderException()

@Throws(NoLoaderException::class, NoPreloadException::class)
fun Class<*>.preload() = if (!inheritsAnnotation(Preload::class.java)) throw NoPreloadException() else stageLoad(classLoader)

@Throws(NoLoaderException::class, NoPreloadException::class)
fun Class<*>.preload(classLoader: ClassLoader) = if (!inheritsAnnotation(Preload::class.java)) throw NoPreloadException() else stageLoad(classLoader)

class NoLoaderException : Exception("Tried to load a class with no class loader")

class NoPreloadException : Exception("Tried to load a class that isn't allowed to be preloaded")

fun Class<*>.inheritsAnnotation(annotation: Class<out Annotation>): Boolean = if (isAnnotationPresent(annotation)) true else interfaces.any { it.inheritsAnnotation(annotation) } || (superclass?.inheritsAnnotation(annotation) ?: false)//////

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

// these two objects recreate access to utility files from the sdk, which copied them from the r8 project:
// https://r8.googlesource.com/r8/
object DescriptorUtils {
	fun guessTypeDescriptor(name: String): String {
		assert(name.endsWith(".class")) { "Name $name must have `.class` suffix" }
		val descriptor = name.substring(0, name.length - ".class".length)
		if (descriptor.indexOf('.') != -1) {
			throw RuntimeException("Unexpected class file name: $name")
		}
		return "L$descriptor;"
	}

	fun getPathFromDescriptor(descriptor: String): String {
		// We are quite loose on names here to support testing illegal names, too.
		assert(descriptor.startsWith("L"))
		assert(descriptor.endsWith(";"))
		return descriptor.substring(
			1,
			descriptor.length - 1
		) + ".class"
	}
}

object ZipUtils {
	fun isClassFile(entry: String): Boolean {
		val name = entry.lowercase(Locale.getDefault())
		if (name.endsWith("module-info.class")) {
			return false
		}
		if (name.startsWith("meta-inf") || name.startsWith("/meta-inf")) {
			return false
		}
		return name.endsWith(".class")
	}

	/**
	 * consumes the zipfile and returns the list of classnames within it
	 */
	fun classNames(zipFile: ZipFile): List<String> = zipFile.run {
		return entries().toList().mapNotNull {
			if (it.isDirectory) return@mapNotNull null
			val name = it.name
			if (!isClassFile(name)) return@mapNotNull null
			name.substring(0, name.length - ".class".length).replace('/', '.')
		}.also {
			close()
		}
	}

	// Added by lizlooney to use instead of com.google.common.io.ByteStreams.toByteArray.
	@Throws(IOException::class)
	fun toByteArray(ins: InputStream): ByteArray {
		val outs = ByteArrayOutputStream()
		copyStream(ins, outs)
		return outs.toByteArray()
	}

	@Throws(IOException::class)
	fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
		val cbBuffer = min(4096.0, inputStream.available().toDouble()).toInt()
		val buffer = ByteArray(cbBuffer)
		while (true) {
			val cbRead = inputStream.read(buffer)
			if (cbRead <= 0) break
			outputStream.write(buffer, 0, cbRead)
		}
	}
}