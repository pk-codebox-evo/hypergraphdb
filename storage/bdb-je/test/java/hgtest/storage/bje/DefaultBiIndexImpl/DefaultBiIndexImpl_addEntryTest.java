package hgtest.storage.bje.DefaultBiIndexImpl;

import org.easymock.EasyMock;
import org.hypergraphdb.HGException;
import org.hypergraphdb.storage.bje.DefaultBiIndexImpl;
import org.hypergraphdb.transaction.HGTransactionManager;
import org.powermock.api.easymock.PowerMock;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Use 'null' comparator in these test cases - it forces
 * {@link org.hypergraphdb.storage.bje.DefaultBiIndexImpl} to use default
 * Sleepycat's BtreeComparator
 * 
 * @author Yuriy Sechko
 */
public class DefaultBiIndexImpl_addEntryTest extends
		DefaultBiIndexImpl_TestBasis
{

	private DefaultBiIndexImpl<Integer, String> indexImpl;

	@Test
	public void keyIsNull() throws Exception
	{
		final Exception expected = new NullPointerException();

		startupIndexImpl();

		try
		{
			indexImpl.addEntry(null, "some string");
		}
		catch (Exception occurred)
		{
			assertEquals(occurred.getClass(), expected.getClass());
			assertEquals(occurred.getMessage(), expected.getMessage());
		}
		finally
		{
			indexImpl.close();
		}
	}

	private void startupIndexImpl() {
		mockStorage();
		PowerMock.replayAll();
		indexImpl = new DefaultBiIndexImpl(INDEX_NAME, storage,
				transactionManager, keyConverter, valueConverter, null);
		indexImpl.open();
	}

	@Test
	public void valueIsNull() throws Exception
	{
		final Exception expected = new NullPointerException();

		startupIndexImpl();

		try
		{
			indexImpl.addEntry(48, null);
		}
		catch (Exception occurred)
		{
			assertEquals(occurred.getClass(), expected.getClass());
			assertEquals(occurred.getMessage(), expected.getMessage());
		}
		finally
		{
			indexImpl.close();
		}
	}

	@Test
	public void addOneEntry() throws Exception
	{
		startupIndexImpl();

		indexImpl.addEntry(1, "one");

		final String stored = indexImpl.getData(1);
		assertEquals(stored, "one");
		indexImpl.close();
	}

	@Test
	public void addSeveralDifferentEntries() throws Exception
	{
		final List<String> expected = new ArrayList<String>();
		expected.add("twenty two");
		expected.add("thirty three");
		expected.add("forty four");

		startupIndexImpl();

		indexImpl.addEntry(22, "twenty two");
		indexImpl.addEntry(33, "thirty three");
		indexImpl.addEntry(44, "forty four");

		final List<String> stored = new ArrayList<String>();
		stored.add(indexImpl.getData(22));
		stored.add(indexImpl.getData(33));
		stored.add(indexImpl.getData(44));

		assertEquals(stored, expected);
		indexImpl.close();
	}

	@Test
	public void addDuplicatedKeys() throws Exception
	{
		final String expected = "second value";

		startupIndexImpl();

		indexImpl.addEntry(4, "first value");
		indexImpl.addEntry(4, "second value");

		final String stored = indexImpl.getData(4);

		assertEquals(stored, expected);
		indexImpl.close();
	}

	@Test
	public void indexIsNotOpened() throws Exception
	{
		final Exception expected = new HGException(
				"Attempting to operate on index 'sample_index' while the index is being closed.");

		PowerMock.replayAll();
		final DefaultBiIndexImpl<Integer, String> indexImplSpecificForThisTestCase = new DefaultBiIndexImpl(
				INDEX_NAME, storage, transactionManager, keyConverter,
				valueConverter, null);
		try
		{
			indexImplSpecificForThisTestCase.addEntry(2, "two");
		}
		catch (Exception occurred)
		{
			assertEquals(occurred.getClass(), expected.getClass());
			assertEquals(occurred.getMessage(), expected.getMessage());
		}
	}

	@Test
	public void transactionManagerThrowsException() throws Exception
	{
		final Exception expected = new HGException(
				"Failed to add entry to index 'sample_index': java.lang.IllegalStateException: Transaction manager is fake.");

		mockStorage();
		HGTransactionManager fakeTransactionManager = PowerMock
				.createStrictMock(HGTransactionManager.class);
		fakeTransactionManager.getContext();
		EasyMock.expectLastCall().andThrow(
				new IllegalStateException("Transaction manager is fake."));
		PowerMock.replayAll();
		final DefaultBiIndexImpl<Integer, String> indexImplSpecificForThisTestCase = new DefaultBiIndexImpl(
				INDEX_NAME, storage, fakeTransactionManager, keyConverter,
				valueConverter, null);
		indexImplSpecificForThisTestCase.open();

		try
		{
			indexImplSpecificForThisTestCase.addEntry(2, "two");
		}
		catch (Exception occurred)
		{
			assertEquals(occurred.getClass(), expected.getClass());
			assertEquals(occurred.getMessage(), expected.getMessage());
		}
		finally
		{
			indexImplSpecificForThisTestCase.close();
		}
	}
}