package net.jirasystems.filestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides helper functions for the {@link FileStore} tests.
 * 
 * @author David Carboni
 * 
 */
final class FileStoreTestUtils {

	/**
	 * No need to instantiate.
	 */
	private FileStoreTestUtils() {
		// No need to instantiate.
	}

	/**
	 * @throws IOException .
	 * @return A new temp folder.
	 */
	public static File createTempFolder() throws IOException {
		File tempFolder = File.createTempFile("FileStoreTest", "store");
		tempFolder.delete();
		tempFolder.mkdir();
		return tempFolder;
	}

	/**
	 * This method recursively deletes a folder. This is intended to delete
	 * temporary folders used in tests. This method prints a message to stdout
	 * if it is not able to delete the given folder. Typically this indicates
	 * that a file in the hierarchy is still locked, probably because a stream
	 * on it was not closed.
	 * 
	 * @param folder
	 *            The root of the folder hierarchy to be deleted.
	 */
	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				deleteFolder(file);
			}
			file.delete();
		}
		if (!folder.delete()) {
			System.out.println("Folder not deleted: " + folder.getPath());
		}
	}

	// This is a relatively random sequence used for generating content files.
	private static byte sequence = (byte) System.currentTimeMillis();

	/**
	 * This method generates a file with some random content in it for use in
	 * testing the file store.
	 * 
	 * @return The generated file.
	 * @throws IOException
	 *             If an error occurs in creating the file.
	 */
	public static File generateContent() throws IOException {

		// Create a source file of about 1K:
		final int minimumSize = 1020;
		int fileLength = minimumSize + sequence++;
		byte[] bytes = new byte[fileLength];
		for (int c = 0; c < bytes.length; c++) {
			bytes[c] = sequence++;
		}
		File file = File.createTempFile("content", "file");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();

		return file;
	}

	/**
	 * This method compares the content of two input streams to verify that they
	 * contain the same data.
	 * 
	 * @param a
	 *            The first input stream.
	 * @param b
	 *            The second input stream.
	 * @return If both input streams contain the same bytes, true. False
	 *         otherwise.
	 * @throws IOException
	 *             If an error occurs in comparing the streams.
	 */
	public static boolean compareContent(InputStream a, InputStream b)
			throws IOException {

		if (a == null || b == null)
			return false;

		boolean result = true;

		// Verify that the two streams are identical
		int i;
		while ((i = a.read()) != -1) {
			result &= i == b.read();
		}
		// Check that both files finished at the same point
		result &= i == b.read();

		return result;
	}
}
