package dev.frozenmilk.sinister.util.log

/**
 * derived from the Android Log mock provided by Android.
 */
open class LoggerImpl: LoggerInterface {
	companion object Priority {
		/**
		 * Priority constant for the println method; use Log.v.
		 */
		const val VERBOSE = "VERBOSE"

		/**
		 * Priority constant for the println method; use Log.d.
		 */
		const val DEBUG = "DEBUG"

		/**
		 * Priority constant for the println method; use Log.i.
		 */
		const val INFO = "INFO"

		/**
		 * Priority constant for the println method; use Log.w.
		 */
		const val WARN = "WARN"

		/**
		 * Priority constant for the println method; use Log.e.
		 */
		const val ERROR = "ERROR"
	}

	/**
	 * Send a [VERBOSE] log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	override fun v(tag: String?, msg: String) = println(VERBOSE, tag, msg)

	/**
	 * Send a [VERBOSE] log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	override fun v(tag: String?, msg: String, tr: Throwable) = println(VERBOSE, tag, "$msg\n${tr.stackTraceToString()}".trimIndent())

	/**
	 * Send a [DEBUG] log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	override fun d(tag: String?, msg: String) = println(DEBUG, tag, msg)

	/**
	 * Send a [DEBUG] log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	override fun d(tag: String?, msg: String, tr: Throwable) = println(DEBUG, tag, "$msg\n${tr.stackTraceToString()}".trimIndent())

	/**
	 * Send an [INFO] log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	override fun i(tag: String?, msg: String) = println(INFO, tag, msg)

	/**
	 * Send a [INFO] log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	override fun i(tag: String?, msg: String, tr: Throwable) = println(INFO, tag, "$msg\n${tr.stackTraceToString()}".trimIndent())

	/**
	 * Send a [WARN] log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	override fun w(tag: String?, msg: String) = println(WARN, tag, msg)

	/**
	 * Send a [WARN] log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	override fun w(tag: String?, msg: String, tr: Throwable) = println(WARN, tag, "$msg\n${tr.stackTraceToString()}".trimIndent())

	/**
	 * Send an [ERROR] log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	override fun e(tag: String?, msg: String) = println(ERROR, tag, msg)

	/**
	 * Send a [ERROR] log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	override fun e(tag: String?, msg: String, tr: Throwable) = println(ERROR, tag, "$msg\n${tr.stackTraceToString()}".trimIndent())

	/**
	 * Low-level logging call.
	 * @param priority The priority/type of this log message
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	open fun println(priority: String, tag: String?, msg: String) {
		println("[$priority]${if (tag != null) " <$tag> " else " "}${msg.replace("\n", "\n\t")}")
	}
}