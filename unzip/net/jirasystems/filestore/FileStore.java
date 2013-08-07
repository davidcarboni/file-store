/**
 * 
 */
package net.jirasystems.filestore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * Provides an interface for storing and retrieving files by arbitrary ID. Files are stored under
 * the <code>basePath</code>, using a binary-tree style structure, based on the ID.
 * 
 * @author david
 * 
 */
public class FileStore {

	/**
	 * This is the default file extension that will be used for files by instances of this class. It
	 * is set at instantiation time. This is necessary so that a folder and file of the same name
	 * can be created in the same directory. This is needed where two IDs start with the same
	 * characters, but one ID is longer than the other, e.g. 10 and 1000. The former creates
	 * "/10.file" and the latter creates "/10/00.file" so that there is no clash on "10". The
	 * default is "{@value #defaultFileExtension}".
	 */
	public static final String defaultFileExtension = ".file";

	/**
	 * This is the default regular expression for checking the validity of IDs that will be used by
	 * instances of this class. It is set at instantiation time. This is necessary in order to rule
	 * out IDs that contain characters which are not valid in filenames. The default is "
	 * {@value #defaultIdRegex}".
	 */
	public static final String defaultIdRegex = "[a-zA-Z0-9_\\.-]+";

	/**
	 * Internally, this class stores files in a tree structure for speed of access. The ID of a file
	 * is broken down into "chunks" that determine the path to the file. This value determines the
	 * length of these chunks. The default is {@value #defaultIdChunkSize}.
	 */
	public static final int defaultIdChunkSize = 2;

	private int idChunkSize = defaultIdChunkSize;
	private String idRegex = defaultIdRegex;
	private String basePath;
	private String extension = defaultFileExtension;
	private Pattern pattern = Pattern.compile(idRegex);

	/**
	 * Default constructor. Performs no initialisation.
	 */
	public FileStore() {
		// Preserve default constructor
	}

	/**
	 * Initialises the instance with default values and the given base path.
	 * 
	 * @param basePath
	 *            The root directory for the repository.
	 */
	public FileStore(String basePath) {
		setBasePath(basePath);
	}

	/**
	 * @param id
	 *            The ID to be validated for suitability to be used to identify a file in the
	 *            filestore.
	 * @return True if the given ID is not null or an empty string and matches the
	 *         <code>idRegex</code>.
	 */
	public boolean validId(String id) {

		// Basic checks:
		if (id == null) {
			return false;
		}
		if (id.length() == 0) {
			return false;
		}

		// Regex validation:
		return pattern.matcher(id).matches();
	}

	/**
	 * 
	 * @param id
	 *            The ID to query.
	 * @return If the given ID exists in the filestore, true. Otherwise, false.
	 */
	public boolean exists(String id) {
		File file = idToFile(id);
		boolean exists = file.exists();
		return exists;
	}

	/**
	 * This method allows you to read a file from the repository. The {@link FileInputStream} is
	 * wrapped with a {@link BufferedInputStream} internally in order that the file can be read
	 * efficiently by default.
	 * 
	 * @param id
	 *            The ID of the file to be accessed.
	 * @return An {@link InputStream} for the specified file, or null if the file does not exist.
	 */
	public InputStream read(String id) {
		File file = idToFile(id);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		return bis;
	}

	/**
	 * This method allows you to create a new file in the file store.
	 * 
	 * @param id
	 *            The ID for the new file.
	 * @param content
	 *            The content for the file.
	 * @throws FileStoreException
	 *             If the ID already exists, or if an IO error occurs.
	 */
	public void create(String id, InputStream content) throws FileStoreException {
		File file = idToFile(id);
		File folder = file.getParentFile();
		folder.mkdirs();
		try {
			// Check that the file doesn't already exist and can be created
			if (!file.createNewFile()) {
				throw new FileStoreException("Duplicate file ID " + id + " (" + file.getPath() + ")");
			}
			writeFile(file, content);
		} catch (IOException e) {
			throw new FileStoreException("Unable to create file for ID " + id + " (" + file.getPath() + ")", e);
		}
	}

	/**
	 * This method allows you to create a new file in the file store and receive back an output
	 * stream in order to write data directly.
	 * <p>
	 * This method is useful when using a method on an object which requires an output stream in
	 * order to save its data e.g. xml marshalling objects often save their data directly to an
	 * output stream.
	 * <p>
	 * This method actually returns a buffered output stream so it is not necessary to wrap the
	 * returned stream.
	 * 
	 * @param id
	 *            The ID for the new file.
	 * @return A new output stream for the given id. The caller is responsible for closing the
	 *         output stream.
	 * @throws FileStoreException
	 *             If the ID already exists, or if an IO error occurs.
	 */
	public OutputStream create(String id) throws FileStoreException {

		OutputStream result;
		File file = idToFile(id);
		File folder = file.getParentFile();
		folder.mkdirs();
		try {
			// Check that the file doesn't already exist and can be created
			if (!file.createNewFile()) {
				throw new FileStoreException("Duplicate file ID " + id + " (" + file.getPath() + ")");
			}
			result = createOutputStream(file);
		} catch (IOException e) {
			throw new FileStoreException("Unable to create file for ID " + id + " (" + file.getPath() + ")", e);
		}

		return result;
	}

	/**
	 * This method allows you to replace the content of the file with the given ID.
	 * 
	 * @param id
	 *            The ID of the file to be updated.
	 * @param content
	 *            The new content for the file.
	 * @throws FileStoreException
	 *             If the content is null, the ID does not exist, or if an error occurs while
	 *             updating the file.
	 */
	public void update(String id, InputStream content) throws FileStoreException {
		if (content == null) {
			throw new FileStoreException("Null content detected.");
		}
		File file = idToFile(id);
		// Check existence directly (for expedience) rather than calling the
		// exists method
		if (!file.exists()) {
			throw new FileStoreException("Unable to find file ID " + id + " (" + file.getPath() + ")");
		}
		try {
			writeFile(file, content);
		} catch (IOException e) {
			throw new FileStoreException("Unable to update file for ID " + id + " (" + file.getPath() + ")", e);
		}
	}

	/**
	 * This method allows you to update the content of the file with the given ID using an output
	 * stream in order to write data directly.
	 * <p>
	 * This method is useful when using a method on an object which requires an output stream in
	 * order to save its data e.g. xml marshalling objects often save their data directly to an
	 * output stream.
	 * <p>
	 * This method actually returns a buffered output stream so it is not necessary to wrap the
	 * returned stream.
	 * 
	 * @param id
	 *            The ID of the file to be updated.
	 * @return A new output stream for the given id. The caller is responsible for closing the
	 *         output stream.
	 * @throws FileStoreException
	 *             If the content is null, the ID does not exist, or if an error occurs while
	 *             updating the file.
	 */
	public OutputStream update(String id) throws FileStoreException {

		OutputStream result;
		File file = idToFile(id);
		// Check existence directly (for expedience) rather than calling the
		// exists method
		if (!file.exists()) {
			throw new FileStoreException("Unable to find file ID " + id + " (" + file.getPath() + ")");
		}
		try {
			result = createOutputStream(file);
		} catch (IOException e) {
			throw new FileStoreException("Unable to update file for ID " + id + " (" + file.getPath() + ")", e);
		}

		return result;
	}

	/**
	 * This method allows you to delete the file associated with the given ID from the file store.
	 * No attempt is made to delete parent folders of the file.
	 * 
	 * @param id
	 *            The ID to be deleted.
	 * @return The return value of this method is governed by {@link File#delete()};
	 * @see File#delete().
	 * @throws FileStoreException
	 *             If the file to be deleted does not exist.
	 */
	public boolean delete(String id) throws FileStoreException {
		File file = idToFile(id);
		// Check existence directly (for expedience) rather than calling the
		// exists method
		if (!file.exists()) {
			throw new FileStoreException("Unable to find file ID " + id + " (" + file.getPath() + ")");
		}
		boolean result = file.delete();
		return result;
	}

	// --------------- Internal methods --------------- //

	/**
	 * Converts the given ID to a path relative to the base path of the file store.
	 * 
	 * @param id
	 *            The ID to be converted to a path.
	 * @return A path relative to the base path of the file store.
	 */
	protected String idToPath(String id) {
		StringBuilder result = new StringBuilder();
		int pos = 0;
		int chunkPos = 0;
		while (pos < id.length()) {
			result.append(id.charAt(pos));
			pos++;
			chunkPos++;
			// Add a file separator at the end of each chunk, unless we have
			// reached the end of the String:
			if ((chunkPos >= idChunkSize) && (pos < id.length())) {
				chunkPos = 0;
				result.append(File.separatorChar);
			}
		}
		// Finally, add the extension:
		return result.toString() + extension;
	}

	/**
	 * Converts an ID into a {@link File} in the file store.
	 * 
	 * @param id
	 *            The ID to be converted.
	 * @return A {@link File} instance representing the file that corresponds to the given ID.
	 */
	protected File idToFile(String id) {
		File result = new File(basePath, idToPath(id));
		return result;
	}

	/**
	 * This method writes the contents of the given {@link InputStream} to the given {@link File}.
	 * If content is null, a {@link NullPointerException} is thrown. A {@link BufferedOutputStream}
	 * is used internally to write to the file, but no wrapping is performed on the incoming content
	 * parameter. As a result, if you wish the reading of content to have any special treatment,
	 * such as being buffered, you need to do this explicitly before invoking this method. This
	 * method does not close the content {@link InputStream}.
	 * 
	 * @param file
	 *            The file to which content will be written (if not null) or which will otherwise be
	 *            created if it does not exist.
	 * @param content
	 *            The content to be written to the file. The caller is responsible for closing the
	 *            stream - this method does not assume that it should be closed.
	 * @throws IOException
	 *             If an error occurs.
	 */
	protected void writeFile(File file, InputStream content) throws IOException {

		if (content == null) {
			throw new NullPointerException("Null content stream.");
		}

		BufferedOutputStream bos = createOutputStream(file);
		int b;
		try {
			while ((b = content.read()) != -1) {
				bos.write(b);
			}
		} finally {
			IOUtils.closeQuietly(bos);
		}
	}

	/**
	 * Creates an output stream for the given file. The caller is responsible for closing the
	 * returned stream.
	 * 
	 * @param file
	 *            The file to create an output stream for
	 * @return A buffered output stream for the given file
	 * @throws IOException
	 *             If it is not possible to create a file output stream for the given file
	 */
	private BufferedOutputStream createOutputStream(File file) throws IOException {

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new IOException("Unable to create output stream for file " + file.getPath());
		}

		BufferedOutputStream bos = new BufferedOutputStream(fos);
		return bos;
	}

	// --------------- Getters and Setters --------------- //

	/**
	 * @return the idRegex
	 */
	public String getIdRegex() {
		return idRegex;
	}

	/**
	 * Sets the regular expression used for validating IDs and compiles a {@link Pattern}. The regex
	 * defaults to [a-zA-Z0-9_\\.-]+ which means that a valid ID consists of one or more letters,
	 * numbers, underscores, dashes and periods.
	 * 
	 * In practice, IDs are first checked for null and empty String, before being validated with the
	 * regex.
	 * 
	 * @param idRegex
	 *            the idRegex to set
	 */
	public void setIdRegex(String idRegex) {
		this.idRegex = idRegex;
		pattern = Pattern.compile(idRegex);
	}

	/**
	 * @return the basePath
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * Sets the root folder that will be used as the starting point for storing files.
	 * 
	 * @param basePath
	 *            the basePath to set
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	/**
	 * @return the idChunkSize
	 */
	public int getIdChunkSize() {
		return idChunkSize;
	}

	/**
	 * The ID chunk size controls how given IDs are split up to create a folder hierarchy in the
	 * binary tree. The default is 2, so for an ID 1234567, this would generate
	 * [basePath]/12/34/56/7.file
	 * 
	 * @param idChunkSize
	 *            the idChunkSize to set
	 */
	public void setIdChunkSize(int idChunkSize) {
		this.idChunkSize = idChunkSize;
	}

	/**
	 * @return the extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Sets the string that will be added to the end of each file. This is important if you need to
	 * avoid collisions between folder and file names, such as IDs 1000 and 10011, which would
	 * create /10/00 and /10/00/11. In the former "00" is a file and in the latter it is a directory
	 * of the same name. Adding an extension prevents these collisions. The extension defaults to
	 * ".file".
	 * 
	 * @param extension
	 *            the extension to set
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}
}
