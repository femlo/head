package org.mifos.framework.persistence;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junitx.framework.ObjectAssert;
import net.sourceforge.mayfly.Database;

import org.mifos.framework.util.helpers.DatabaseSetup;

public class DatabaseVersionPersistenceTest extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		DatabaseSetup.configureLogging();
		DatabaseSetup.initializeHibernate();
	}

	public void testRead() throws Exception {
		new DatabaseVersionPersistence().read();
	}
	
	public void testReadSuccess() throws Exception {
		Database database = new Database();
		database.execute("create table DATABASE_VERSION(DATABASE_VERSION INTEGER)");
		database.execute("insert into DATABASE_VERSION(DATABASE_VERSION) VALUES(53)");
		new DatabaseVersionPersistence().read(database.openConnection());
	}
	
	public void testReadTwoRows() throws Exception {
		Database database = new Database();
		database.execute("create table DATABASE_VERSION(DATABASE_VERSION INTEGER)");
		database.execute("insert into DATABASE_VERSION(DATABASE_VERSION) VALUES(53)");
		database.execute("insert into DATABASE_VERSION(DATABASE_VERSION) VALUES(54)");
		try {
			new DatabaseVersionPersistence().read(database.openConnection());
			fail();
		}
		catch (RuntimeException e) {
			assertEquals("too many rows in DATABASE_VERSION", e.getMessage());
		}
	}
	
	public void testReadNoRows() throws Exception {
		Database database = new Database();
		database.execute("create table DATABASE_VERSION(DATABASE_VERSION INTEGER)");
		try {
			new DatabaseVersionPersistence().read(database.openConnection());
			fail();
		}
		catch (RuntimeException e) {
			assertEquals("No row in DATABASE_VERSION", e.getMessage());
		}
	}
	
	public void testReadNoTable() throws Exception {
		/* This is the case where the user has an old database (from before
		   version 100).  They will need to upgrade to 100 manually.  */
		Database database = new Database();
		try {
			new DatabaseVersionPersistence().read(database.openConnection());
			fail();
		}
		catch (SQLException e) {
		}
	}
	
	public void testWrite() throws Exception {
		new DatabaseVersionPersistence().write(77);
		assertEquals(77, 
			new DatabaseVersionPersistence().read());
	}

	public void testIsVersioned() throws Exception {
		assertTrue(new DatabaseVersionPersistence().isVersioned());
	}
	
	/* not repeatable
	public void testNotIsVersioned() throws Exception {
		DatabaseVersionPersistence dvp = new DatabaseVersionPersistence();
		Connection c =  dvp.getConnection();
		c.createStatement().executeUpdate("drop table DATABASE_VERSION");
		c.commit();
		assertFalse(new DatabaseVersionPersistence().isDBVersioned());
		
	}
	*/
	
	public void testNoUpgrade() throws Exception {
		DatabaseVersionPersistence persistence = new DatabaseVersionPersistence();
		List<Upgrade> scripts = persistence.scripts(88, 88);
		assertEquals(0, scripts.size());
	}

	public void testUpgrade() throws Exception {
		DatabaseVersionPersistence persistence = new DatabaseVersionPersistence() {
			@Override
			URL lookup(String name) {
				if ("upgrade_to_89.sql".equals(name) 
					|| "upgrade_to_90.sql".equals(name)) {
					try {
						return new URL("file:" + name);
					} catch (MalformedURLException e) {
						throw (AssertionFailedError)new AssertionFailedError().initCause(e);
					}
				}
				else {
					throw new AssertionFailedError("got unexpected " + name);
				}
			}
		};
		List<Upgrade> scripts = persistence.scripts(90, 88);
		assertEquals(2, scripts.size());
		assertEquals("upgrade_to_89.sql", scripts.get(0).sql().getPath());
		assertEquals("upgrade_to_90.sql", scripts.get(1).sql().getPath());
	}

	/*
	Like the above test, but with files instead of overriding lookup.
	
	public void testUpgradeWithFile() throws Exception {
		DatabaseVersionPersistence persistence = new DatabaseVersionPersistence();
		URL[] scripts = persistence.scripts(90, 88);
		assertEquals(2, scripts.length);
		scripts[0].openStream().close();
		scripts[1].openStream().close();
	}
	*/

	public void testDowngrade() throws Exception {
		DatabaseVersionPersistence persistence = new DatabaseVersionPersistence();
		try {
			persistence.scripts(87, 88);
			fail();
		}
		catch (UnsupportedOperationException e) {
			assertEquals(
				"your database needs to be downgraded from 88 to 87", 
				e.getMessage());
		}
	}
	
	public void testReadEmpty() throws Exception {
		String[] sqlStatements = SqlUpgrade.readFile(
						new ByteArrayInputStream(new byte[0]));
		assertEquals(0, sqlStatements.length);
	}

	public void testBadUtf8() throws Exception {
		try {
			SqlUpgrade.readFile(
					new ByteArrayInputStream(new byte[] { (byte)0x80 }));
			fail();
		}
		catch (RuntimeException e) {
			ObjectAssert.assertInstanceOf(CharacterCodingException.class, e.getCause());
		}
	}
	
	public void testGoodUtf8() throws Exception {
		String[] sqlStatements = SqlUpgrade.readFile(
				new ByteArrayInputStream(new byte[] {
						(byte)0xe2, (byte)0x82, (byte)0xac }));
		assertEquals(1, sqlStatements.length);
		String euroSign = sqlStatements[0];
		assertEquals("\n\u20AC", euroSign);
	}

	public void testExecuteStream() throws Exception {
		SqlUpgrade persistence = new SqlUpgrade(null);
		Connection conn = new Database().openConnection();
		byte[] sql = (
				"create table FOO(DATABASE_VERSION INTEGER);\n"+
				"--some comment\n"+
				"insert into FOO(DATABASE_VERSION) VALUES(53);\n"
				).getBytes("UTF-8");
		ByteArrayInputStream in = new ByteArrayInputStream(sql);
		persistence.execute(in, conn);
		conn.commit();
		readOneValueFromFoo(conn);
	}

	public void testUpgradeDatabase() throws Exception {
		DatabaseVersionPersistence persistence = new DatabaseVersionPersistence();
		Database database = new Database();
		database.execute("create table DATABASE_VERSION(DATABASE_VERSION INTEGER)");
		database.execute("insert into DATABASE_VERSION(DATABASE_VERSION) VALUES(78)");
		Connection conn = database.openConnection();
		conn.setAutoCommit(false);
		persistence.upgradeDatabase(conn, 80);
		conn.commit();
		
		readOneValueFromFoo(conn);
	}

	private void readOneValueFromFoo(Connection conn) throws SQLException {
		Statement statement = conn.createStatement();
		ResultSet results = statement.executeQuery("select * from FOO");
		assertTrue(results.next());
		int valueFromFoo = results.getInt(1);
		assertEquals(53, valueFromFoo);
		assertFalse(results.next());
		results.close();
		statement.close();
	}
	
}
