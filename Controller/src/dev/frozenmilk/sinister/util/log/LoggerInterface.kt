package dev.frozenmilk.sinister.util.log

interface LoggerInterface {
	/**
	 * Send a VERBOSE log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	fun v(tag: String?, msg: String)

	/**
	 * Send a VERBOSE log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	fun v(tag: String?, msg: String, tr: Throwable)

	/**
	 * Send a DEBUG log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	fun d(tag: String?, msg: String)

	/**
	 * Send a DEBUG log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	fun d(tag: String?, msg: String, tr: Throwable)

	/**
	 * Send an INFO log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	fun i(tag: String?, msg: String)

	/**
	 * Send a INFO log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	fun i(tag: String?, msg: String, tr: Throwable)

	/**
	 * Send a WARN log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	fun w(tag: String?, msg: String)

	/**
	 * Send a WARN log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	fun w(tag: String?, msg: String, tr: Throwable)

	/**
	 * Send an ERROR log message.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	fun e(tag: String?, msg: String)

	/**
	 * Send a ERROR log message and log the exception.
	 * @param tag Used to identify the source of a log message.  It usually identifies
	 * the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 * @param tr An exception to log
	 */
	fun e(tag: String?, msg: String, tr: Throwable)
}