/**
 * 
 */
package net.jirasystems.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author david
 * 
 */
public class MultithreadedFilestoreTest {

	private static File tempFolder;
	private static FileStore fileStore;
	private AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

	/**
	 * @throws java.lang.Exception .
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		tempFolder = FileStoreTestUtils.createTempFolder();
		fileStore = new FileStore(tempFolder.getPath());
		System.out.println(fileStore.getBasePath());
	}

	/**
	 * @throws java.lang.Exception .
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileStoreTestUtils.deleteFolder(tempFolder);
	}

	// /**
	// * @throws java.lang.Exception
	// */
	// @Before
	// public void setUp() throws Exception {
	// }
	//
	// /**
	// * @throws java.lang.Exception
	// */
	// @After
	// public void tearDown() throws Exception {
	// }

	private Exception exception;

	/**
	 * Test method for trying out multi-threaded access to the file store.
	 */
	@Test
	public void testMultipleThreads() {

		final int trials = 500;

		for (int i = 0; i < trials; i++) {
			Runnable runnable = new Runnable() {

				public void run() {
					try {
						testCreateUpdateDelete();
					} catch (Exception e) {
						exception = e;
					}
				}
			};
			Thread thread = new Thread(runnable);
			thread.start();
		}

		if (exception != null) {
			exception.printStackTrace();
		}
		Assert.assertNull(exception);
	}

	/**
	 * Test method to be called by each thread being tested.
	 * 
	 * @throws IOException
	 *             If an error occurs.
	 * @throws FileStoreException
	 *             If an error occurs.
	 */
	public void testCreateUpdateDelete() throws IOException, FileStoreException {

		// Generate test data
		long idLong = atomicLong.getAndIncrement();
		String id = String.valueOf(idLong);
		File contentStart = FileStoreTestUtils.generateContent();
		File contentUpdated = FileStoreTestUtils.generateContent();

		// Create stage
		InputStream create = new FileInputStream(contentStart);
		fileStore.create(id, create);
		create.close();
		InputStream a = new FileInputStream(contentStart);
		InputStream b = fileStore.read(id);
		Assert.assertTrue(fileStore.exists(id));
		Assert.assertTrue(FileStoreTestUtils.compareContent(a, b));

		// Update stage
		InputStream update = new FileInputStream(contentUpdated);
		fileStore.update(id, update);
		update.close();
		a = new FileInputStream(contentUpdated);
		b = fileStore.read(id);
		Assert.assertTrue(fileStore.exists(id));
		Assert.assertTrue(FileStoreTestUtils.compareContent(a, b));

		// delete stage
		fileStore.delete(id);
		Assert.assertFalse(fileStore.exists(id));
	}
}
