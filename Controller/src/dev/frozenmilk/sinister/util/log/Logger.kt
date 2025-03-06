package dev.frozenmilk.sinister.util.log

import dev.frozenmilk.sinister.configurable.Configurable

@Suppress("unused")
object Logger: Configurable {
	var DELEGATE: LoggerInterface = LoggerImpl()
	/**
	 * Send a VERBOSE log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	@JvmStatic
	fun v(tag: String?, msg: String) = DELEGATE.v(tag, msg)
	/**
	 * Send a VERBOSE log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	@JvmStatic
	fun v(tag: String?, msg: String, tr: Throwable) = DELEGATE.v(tag, msg, tr)
	/**
	 * Send a DEBUG log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	@JvmStatic
	fun d(tag: String?, msg: String) = DELEGATE.d(tag, msg)
	/**
	 * Send a DEBUG log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	@JvmStatic
	fun d(tag: String?, msg: String, tr: Throwable) = DELEGATE.d(tag, msg, tr)
	/**
	 * Send an INFO log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	@JvmStatic
	fun i(tag: String?, msg: String) = DELEGATE.i(tag, msg)
	/**
	 * Send a INFO log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	@JvmStatic
	fun i(tag: String?, msg: String, tr: Throwable) = DELEGATE.i(tag, msg, tr)
	/**
	 * Send a WARN log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	@JvmStatic
	fun w(tag: String?, msg: String) = DELEGATE.w(tag, msg)
	/**
	 * Send a WARN log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	@JvmStatic
	fun w(tag: String?, msg: String, tr: Throwable) = DELEGATE.w(tag, msg, tr)
	/**
	 * Send an ERROR log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	@JvmStatic
	fun e(tag: String?, msg: String) = DELEGATE.e(tag, msg)
	/**
	 * Send a ERROR log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	@JvmStatic
	fun e(tag: String?, msg: String, tr: Throwable) = DELEGATE.e(tag, msg, tr)
}