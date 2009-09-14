package pool;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import org.vcell.util.document.UserInfo;

import cbit.vcell.modeldb.UserTable;

public class JDCConnection implements Connection {

	private JDCConnectionPool pool;

	private Connection conn;

	private boolean inuse;

	private boolean failed;

	private long timestamp;

	private String failedMessage = null;

	public JDCConnection(Connection conn, JDCConnectionPool pool) {
		this.conn = conn;
		this.pool = pool;
		this.inuse = false;
		this.failed = false;
		this.failedMessage = null;
		this.timestamp = 0;
	}

	public void clearWarnings() throws SQLException {
		conn.clearWarnings();
	}

	public void close() throws SQLException {
		if (isFailed()) {
			pool.returnFailedConnection(this);
		} else {
			pool.returnConnection(this);
		}
	}

	public void commit() throws SQLException {
		conn.commit();
	}

	public Statement createStatement() throws SQLException {
		return conn.createStatement();
	}

	/**
	 * createStatement method comment.
	 */
	public java.sql.Statement createStatement(int resultSetType,
			int resultSetConcurrency) throws java.sql.SQLException {
		return conn.createStatement(resultSetType, resultSetConcurrency);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	protected void expireLease() {
		inuse = false;
	}

	/**
	 * This method was created in VisualAge.
	 * 
	 * @param failureMessage
	 *            java.lang.String
	 */
	public void failed(String argFailureMessage) {
		this.failed = true;
		this.failedMessage = argFailureMessage;
	}

	public boolean getAutoCommit() throws SQLException {
		return conn.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return conn.getCatalog();
	}

	protected Connection getConnection() {
		return conn;
	}

	/**
	 * This method was created in VisualAge.
	 * 
	 * @return java.lang.String
	 */
	public String getFailureMessage() {
		return failedMessage;
	}

	public int getHoldability() throws SQLException {
		// TODO check into this
		return conn.getHoldability();
	}

	public long getLastUse() {
		return timestamp;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return conn.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return conn.getTransactionIsolation();
	}

	/**
	 * getTypeMap method comment.
	 */
	public java.util.Map<String, Class<?>> getTypeMap() throws java.sql.SQLException {
		return conn.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return conn.getWarnings();
	}

	public boolean inUse() {
		return inuse;
	}

	public boolean isClosed() throws SQLException {
		return conn.isClosed();
	}

	/**
	 * This method was created in VisualAge.
	 * 
	 * @return boolean
	 */
	public boolean isFailed() {
		return failed;
	}

	public boolean isReadOnly() throws SQLException {
		return conn.isReadOnly();
	}

	public synchronized boolean lease() {
		if (inuse) {
			return false;
		} else {
			inuse = true;
			timestamp = System.currentTimeMillis();
			return true;
		}
	}

	public String nativeSQL(String sql) throws SQLException {
		return conn.nativeSQL(sql);
	}

	/**
	 * prepareCall method comment.
	 */
	public java.sql.CallableStatement prepareCall(java.lang.String sql,
			int resultSetType, int resultSetConcurrency)
			throws java.sql.SQLException {
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return conn.prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * prepareStatement method comment.
	 */
	public java.sql.PreparedStatement prepareStatement(java.lang.String sql,
			int resultSetType, int resultSetConcurrency)
			throws java.sql.SQLException {
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return conn.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return conn.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return conn.prepareStatement(sql, columnNames);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		// TODO Look into this
		conn.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		conn.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		conn.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		conn.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		conn.setCatalog(catalog);
	}

	public void setHoldability(int holdability) throws SQLException {
		// TODO look into this
		conn.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		conn.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		// TODO check into this
		return conn.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		// TODO check into this
		return conn.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		conn.setTransactionIsolation(level);
	}

	/**
	 * setTypeMap method comment.
	 */
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		conn.setTypeMap(map);
	}

	/**
	 * This method was created in VisualAge.
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		return "JDCConnection@" + hashCode() + "(inUse=" + inuse + ", failed="
				+ failed + ", timestamp=" + timestamp + ")";
	}

	public boolean validate() {
		try {
			//System.out.println("testing metadata...");
			DatabaseMetaData dmd = conn.getMetaData();
			//System.out.println("testing metadata...OK");
		} catch (Exception e) {
			System.out.println("testing metadata...failed");
			e.printStackTrace(System.out);
			return false;
		}			
		try {
			//System.out.println("testing autocommit...");
			conn.getAutoCommit();
			//System.out.println("testing autocommit...OK");
		} catch (Exception e) {
			System.out.println("testing autocommit...failed");
			e.printStackTrace(System.out);
			return false;
		}			
			
		try {
			//System.out.println("query user table...");
			String sql = "SELECT * from " + UserTable.table.getTableName() + 
					" WHERE " + UserTable.table.id + "=0";

			Statement stmt = conn.createStatement();
			try {
				ResultSet rset = stmt.executeQuery(sql);
				if (rset.next()){
					UserInfo userInfo = UserTable.table.getUserInfo(rset);
				}
			} finally {
				stmt.close();
			}
					
			//System.out.println("query user table...OK");
		} catch (Exception e) {
			System.out.println("query user table...failed");
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return conn.createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException {
		return conn.createBlob();
	}

	public Clob createClob() throws SQLException {
		return conn.createClob();
	}

	public NClob createNClob() throws SQLException {
		return conn.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return conn.createSQLXML();
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return conn.createStruct(typeName, attributes);
	}

	public Properties getClientInfo() throws SQLException {
		return conn.getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException {
		return conn.getClientInfo(name);
	}

	public boolean isValid(int timeout) throws SQLException {
		return conn.isValid(timeout);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		conn.setClientInfo(properties);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		conn.setClientInfo(name, value);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return conn.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return conn.unwrap(iface);
	}
}
