/**
 * 
 */
package net.jirasystems.filestore;

/**
 * @author david
 * 
 */
public class FileStoreException extends Exception {

	/**
	 * Generated by Eclipse.
	 */
	private static final long serialVersionUID = -5608951761527652849L;

	/**
	 * @param message
	 *            Exception message.
	 */
	public FileStoreException(String message) {
		super(message);
	}

	/**
	 * @param message
	 *            Exception message.
	 * @param cause
	 *            Exception cause
	 */
	public FileStoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
