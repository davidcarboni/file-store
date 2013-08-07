/**
 * 
 */
package net.jirasystems.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author david
 * 
 */
public class FileStoreTest {

	private static FileStore fileStore = new FileStore(); // Coverage for
	// default
	// constructor
	private static File tempFolder;

	/**
	 * @throws java.lang.Exception .
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tempFolder = FileStoreTestUtils.createTempFolder();
		fileStore = new FileStore(tempFolder.getPath());
	}

	/**
	 * @throws java.lang.Exception .
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileStoreTestUtils.deleteFolder(tempFolder);
	}

	// /**
	// * @throws java.lang.Exception .
	// */
	// @Before
	// public void setUp() throws Exception {
	// }
	//
	// /**
	// * @throws java.lang.Exception .
	// */
	// @After
	// public void tearDown() throws Exception {
	// }

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#validId(java.lang.String)}.
	 */
	@Test
	public void testValidId() {
		String[] ids = new String[] {null, "", "aoeui", "12345", "a-_", "&"};
		boolean[] results = new boolean[] {false, false, true, true, true, false};

		for (int i = 0; i < ids.length; i++) {
			Assert.assertEquals(results[i], fileStore.validId(ids[i]));
		}
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#exists(java.lang.String)}.
	 * 
	 * @throws FileStoreException .
	 * @throws IOException .
	 */
	@Test
	public void testExists() throws FileStoreException, IOException {

		// Given
		String id = "testExists";
		Assert.assertFalse(fileStore.exists(id));

		// When
		File file = fileStore.idToFile(id);
		file.getParentFile().mkdirs();
		Assert.assertTrue(file.createNewFile());

		// Then
		Assert.assertTrue(fileStore.exists(id));
		fileStore.delete(id);
		Assert.assertFalse(fileStore.exists(id));
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#read(java.lang.String)}. Test
	 * reading an invalid id.
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test
	public void testReadInvalid() throws IOException, FileStoreException {
		String id = "testRead";

		InputStream is = fileStore.read(id);
		Assert.assertNull(is);
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#read(java.lang.String)}.
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test
	public void testRead() throws IOException, FileStoreException {

		String id = "testRead";

		File file1 = FileStoreTestUtils.generateContent();
		FileInputStream content1 = new FileInputStream(file1);
		fileStore.create(id, content1);
		content1.close();

		InputStream stored = fileStore.read(id);
		Assert.assertNotNull(stored);
		content1 = new FileInputStream(file1);

		Assert.assertTrue(FileStoreTestUtils.compareContent(content1, stored));

		content1.close();
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#read(java.lang.String)}.
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test
	public void testReadDifferentFiles() throws IOException, FileStoreException {

		String id = "testReadDifferentFiles";

		File file1 = FileStoreTestUtils.generateContent();
		FileInputStream content1 = new FileInputStream(file1);
		fileStore.create(id, content1);
		content1.close();

		File file2 = FileStoreTestUtils.generateContent();
		FileInputStream content2 = new FileInputStream(file2);
		fileStore.update(id, content2);
		content2.close();

		InputStream stored = fileStore.read(id);
		content1 = new FileInputStream(file1);
		content2 = new FileInputStream(file2);

		Assert.assertTrue(FileStoreTestUtils.compareContent(content2, stored));
		Assert.assertFalse(FileStoreTestUtils.compareContent(content1, stored));

		content1.close();
		content2.close();
	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#create(java.lang.String, java.io.InputStream)} .
	 * 
	 * @throws FileStoreException .
	 * @throws IOException .
	 */
	@Test
	public void testCreateStringInputStream() throws FileStoreException, IOException {
		String id = "testCreateStringInputStream";
		Assert.assertFalse(fileStore.exists(id));

		File file = FileStoreTestUtils.generateContent();
		FileInputStream fileInputStream = new FileInputStream(file);
		try {
			fileStore.create(id, fileInputStream);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}

		Assert.assertTrue(fileStore.exists(id));
	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#create(java.lang.String, java.io.InputStream)} .
	 * 
	 * @throws FileStoreException .
	 * @throws IOException .
	 */
	@Test
	public void testCreateStringInputStreamFail() throws FileStoreException, IOException {
		String id = "testCreateStringInputStreamFail";
		Assert.assertFalse(fileStore.exists(id));

		File file = fileStore.idToFile(id);
		file.getParentFile().mkdirs();
		Assert.assertTrue(file.createNewFile());

		Assert.assertTrue(fileStore.exists(id));
		try {
			fileStore.create(id, null);
			Assert.fail("Expected exception when creating duplicate ID " + id);
		} catch (FileStoreException e) {
			// Expected.
			System.out.println("");
		}
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#create(java.lang.String)} .
	 * 
	 * @throws FileStoreException .
	 * @throws IOException .
	 */
	@Test
	public void testCreateString() throws FileStoreException, IOException {
		String id = "testCreateString";
		Assert.assertFalse(fileStore.exists(id));

		OutputStream result = fileStore.create(id);
		result.close();

		Assert.assertTrue(fileStore.exists(id));
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#create(java.lang.String)} .
	 * 
	 * @throws FileStoreException .
	 * @throws IOException .
	 */
	@Test
	public void testCreateStringFail() throws FileStoreException, IOException {
		String id = "testCreateStringFail";
		Assert.assertFalse(fileStore.exists(id));

		File file = fileStore.idToFile(id);
		file.getParentFile().mkdirs();
		Assert.assertTrue(file.createNewFile());

		Assert.assertTrue(fileStore.exists(id));
		try {
			fileStore.create(id);
			Assert.fail("Expected exception when creating duplicate ID " + id);
		} catch (FileStoreException e) {
			// Expected.
			System.out.println("");
		}
	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#update(java.lang.String, java.io.InputStream)} .
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test
	public void testUpdateStringInputStream() throws IOException, FileStoreException {

		String id = "testUpdateStringInputStream";

		File file1 = FileStoreTestUtils.generateContent();
		FileInputStream content1 = new FileInputStream(file1);
		fileStore.create(id, content1);
		content1.close();

		File file2 = FileStoreTestUtils.generateContent();
		FileInputStream content2 = new FileInputStream(file2);
		fileStore.update(id, content2);
		content2.close();

		InputStream stored = fileStore.read(id);
		content1 = new FileInputStream(file1);
		content2 = new FileInputStream(file2);

		Assert.assertTrue(FileStoreTestUtils.compareContent(content2, stored));
		Assert.assertFalse(FileStoreTestUtils.compareContent(content1, stored));

		content1.close();
		content2.close();
	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#update(java.lang.String, java.io.InputStream)} .
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test(expected = FileStoreException.class)
	public void testUpdateStringInputStreamNull() throws IOException, FileStoreException {

		String id = "testUpdateStringInputStreamNull";

		File file1 = FileStoreTestUtils.generateContent();
		FileInputStream content1 = new FileInputStream(file1);
		fileStore.create(id, content1);
		content1.close();

		// Test updating with null content
		fileStore.update(id, null);

	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#update(java.lang.String, java.io.InputStream)} .
	 * <p>
	 * Test updating an invalid id
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test(expected = FileStoreException.class)
	public void testUpdateStringInputStreamInvalidId() throws IOException, FileStoreException {

		String id = "testUpdateStringInputStreamInvalidId";
		Assert.assertFalse(fileStore.exists(id));

		File file1 = FileStoreTestUtils.generateContent();
		FileInputStream content1 = new FileInputStream(file1);

		// Test updating a nonexistent ID
		content1 = new FileInputStream(file1);
		try {
			fileStore.update(id, content1);
		} finally {
			content1.close();
		}
	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#update(java.lang.String, java.io.InputStream)} .
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test
	public void testUpdateString() throws IOException, FileStoreException {

		String id = "testUpdateString";

		// Create an item in the filestore
		OutputStream outputStream = fileStore.create(id);

		// Populate the content
		File file1 = FileStoreTestUtils.generateContent();
		FileInputStream content1 = new FileInputStream(file1);
		int b;
		try {
			while ((b = content1.read()) != -1) {
				outputStream.write(b);
			}
		} finally {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(content1);
		}

		// Update the item in the filestore
		OutputStream outputStream2 = fileStore.update(id);
		File file2 = FileStoreTestUtils.generateContent();
		FileInputStream content2 = new FileInputStream(file2);
		try {
			while ((b = content2.read()) != -1) {
				outputStream2.write(b);
			}
		} finally {
			IOUtils.closeQuietly(outputStream2);
			IOUtils.closeQuietly(content2);
		}
		content2.close();

		InputStream stored = fileStore.read(id);
		content1 = new FileInputStream(file1);
		content2 = new FileInputStream(file2);

		Assert.assertTrue(FileStoreTestUtils.compareContent(content2, stored));
		Assert.assertFalse(FileStoreTestUtils.compareContent(content1, stored));

		content1.close();
		content2.close();
	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#update(java.lang.String, java.io.InputStream)} .
	 * <p>
	 * Test updating an invalid id
	 * 
	 * @throws IOException .
	 * @throws FileStoreException .
	 */
	@Test(expected = FileStoreException.class)
	public void testUpdateStringInvalidId() throws IOException, FileStoreException {

		String id = "testUpdateStringInvalidId";
		Assert.assertFalse(fileStore.exists(id));

		File file1 = FileStoreTestUtils.generateContent();
		FileInputStream content1 = new FileInputStream(file1);

		// Test updating a nonexistent ID
		content1 = new FileInputStream(file1);
		try {
			fileStore.update(id, content1);
		} finally {
			content1.close();
		}
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#delete(java.lang.String)}.
	 * 
	 * @throws FileStoreException .
	 * @throws IOException .
	 */
	@Test
	public void testDelete() throws FileStoreException, IOException {
		String id = "testDelete";

		File file = fileStore.idToFile(id);
		file.getParentFile().mkdirs();
		Assert.assertTrue(file.createNewFile());

		Assert.assertTrue(fileStore.exists(id));
		fileStore.delete(id);
		Assert.assertFalse(fileStore.exists(id));
		try {
			fileStore.delete(id);
			Assert.fail("Expected an exception when attempting to delete nonexistent ID " + id);
		} catch (FileStoreException e) {
			// Expected
			System.out.println("");
		}
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#idToPath(java.lang.String)}.
	 */
	@Test
	public void testIdToPath() {
		String extension = ".extension";
		fileStore.setExtension(extension);
		String[] ids = new String[] {"testIdToPath", "FileStore", "java.lang.String"};
		String[] paths = new String[] {"te/st/Id/To/Pa/th" + extension, "Fi/le/St/or/e" + extension,
				"ja/va/.l/an/g./St/ri/ng" + extension};
		for (int i = 0; i < ids.length; i++) {
			Assert.assertEquals(paths[i], paths[i]);
		}
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#idToFile(java.lang.String)}.
	 */
	@Test
	public void testIdToFile() {
		String extension = ".extension";
		fileStore.setExtension(extension);
		String[] ids = new String[] {"testIdToPath", "FileStore", "java.lang.String"};
		String[] paths = new String[] {"te/st/Id/To/Pa/th" + extension, "Fi/le/St/or/e" + extension,
				"ja/va/.l/an/g./St/ri/ng" + extension};
		for (int i = 0; i < ids.length; i++) {
			String expectedPath = new File(fileStore.getBasePath(), paths[i]).getPath();
			String actualPath = fileStore.idToFile(ids[i]).getPath();
			System.out.println(expectedPath);
			System.out.println(expectedPath);
			System.out.println("=");
			Assert.assertEquals(expectedPath, actualPath);
		}
	}

	/**
	 * Test method for
	 * {@link net.jirasystems.filestore.FileStore#writeFile(java.io.File, java.io.InputStream)} .
	 * 
	 * @throws FileStoreException .
	 * @throws IOException .
	 */
	@Test
	public void testWriteFile() throws FileStoreException, IOException {

		// Create a source file
		File source = FileStoreTestUtils.generateContent();

		// Test with null
		try {
			fileStore.writeFile(source, null);
		} catch (NullPointerException e) {
			// Expected.
			System.out.println("");
		}

		// Write the source file to a destination file, using the repository
		// method
		FileInputStream content = new FileInputStream(source);
		File destination = File.createTempFile("testWriteFile", "test");
		fileStore.writeFile(destination, content);
		content.close();

		// Verify that the input and output files are identical
		FileInputStream in = new FileInputStream(source);
		FileInputStream out = new FileInputStream(destination);
		Assert.assertTrue(FileStoreTestUtils.compareContent(in, out));
		in.close();
		out.close();

		// Finally, verify we can't write to a nonexistent file:
		Assert.assertTrue(destination.delete());
		Assert.assertTrue(destination.mkdir());
		content = new FileInputStream(source);
		try {
			fileStore.writeFile(destination, content);
			Assert.fail("Expected not to be able to write to a nonexistent file.");
		} catch (IOException e) {
			// Expected.
			System.out.println("");
		}
		content.close();

		source.delete();
		destination.delete();
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#getIdRegex()},
	 * {@link net.jirasystems.filestore.FileStore#setIdRegex(java.lang.String)}.
	 */
	@Test
	public void testIdRegex() {
		String idRegex = "testIdRegex";
		FileStore fileStore = new FileStore();
		fileStore.setIdRegex(idRegex);
		Assert.assertEquals(idRegex, fileStore.getIdRegex());
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#getBasePath()} ,
	 * {@link net.jirasystems.filestore.FileStore#setBasePath(java.lang.String)} .
	 */
	@Test
	public void testBasePath() {
		String basePath = "testBasePath";
		FileStore fileStore = new FileStore();
		fileStore.setBasePath(basePath);
		Assert.assertEquals(basePath, fileStore.getBasePath());
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#getIdChunkSize()},
	 * {@link net.jirasystems.filestore.FileStore#setIdChunkSize(int)}.
	 */
	@Test
	public void testIdChunkSize() {
		final int idChunkSize = 5;
		FileStore fileStore = new FileStore();
		fileStore.setIdChunkSize(idChunkSize);
		Assert.assertEquals(idChunkSize, fileStore.getIdChunkSize());
	}

	/**
	 * Test method for {@link net.jirasystems.filestore.FileStore#getExtension()},
	 * {@link net.jirasystems.filestore.FileStore#setExtension(java.lang.String)} .
	 */
	@Test
	public void testExtension() {
		String extension = "testExtension";
		FileStore fileStore = new FileStore();
		fileStore.setExtension(extension);
		Assert.assertEquals(extension, fileStore.getExtension());
	}

}
