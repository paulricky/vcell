package cbit.vcell.modeldb;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.io.IOException;
import java.sql.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.vcell.util.DataAccessException;
import org.vcell.util.ObjectNotFoundException;
import org.vcell.util.PropertyLoader;
import org.vcell.util.SessionLog;
import org.vcell.util.document.KeyValue;
import org.vcell.util.document.User;

import cbit.sql.*;
import cbit.sql.UserInfo;
/**
 * This type was created in VisualAge.
 */
public class UserDbDriver /*extends DbDriver*/ {
	public static final UserTable userTable = UserTable.table;
	private SessionLog log = null;
/**
 * LocalDBManager constructor comment.
 */
public UserDbDriver(SessionLog sessionLog) {
	super();
	this.log = sessionLog;
}
/**
 * This method was created in VisualAge.
 * @return int
 * @param user java.lang.String
 * @param imageName java.lang.String
 */
public User getUserFromUserid(Connection con, String userid) throws SQLException {
	Statement stmt;
	String sql;
	ResultSet rset;
	log.print("UserDbDriver.getUserFromUserid(userid=" + userid + ")");
	sql = 	"SELECT " + UserTable.table.id + 
			" FROM " + userTable.getTableName() + 
			" WHERE " + UserTable.table.userid + " = '" + userid + "'";
			
	//System.out.println(sql);
	stmt = con.createStatement();
	User user = null;
	try {
		rset = stmt.executeQuery(sql);
		if (rset.next()) {
			KeyValue userKey = new KeyValue(rset.getBigDecimal(UserTable.table.id.toString()));
			user = new User(userid, userKey);
		}
	} finally {
		stmt.close();
	}
	return user;
}
/**
 * This method was created in VisualAge.
 * @return int
 * @param user java.lang.String
 * @param imageName java.lang.String
 */
public User getUserFromUseridAndPassword(Connection con, String userid, String password) throws SQLException {
	Statement stmt;
	String sql;
	ResultSet rset;
	log.print("UserDbDriver.getUserFromUseridAndPassword(userid='" + userid + "', password='"+password+"')");
	sql = 	"SELECT " + UserTable.table.id + 
			" FROM " + userTable.getTableName() + 
			" WHERE " + UserTable.table.userid + " = '" + userid + "'" +
			" AND " + UserTable.table.password + " = '" + password + "'";
			
	//System.out.println(sql);
	stmt = con.createStatement();
	User user = null;
	try {
		rset = stmt.executeQuery(sql);
		if (rset.next()) {
			KeyValue userKey = new KeyValue(rset.getBigDecimal(UserTable.table.id.toString()));
			user = new User(userid, userKey);
		}
	} finally {
		stmt.close();
	}
	return user;
}

public void sendLostPassword(Connection con,String userid) throws SQLException, DataAccessException, ObjectNotFoundException {
	User user = getUserFromUserid(con, userid);
	if(user == null){
		throw new ObjectNotFoundException("User name "+userid+" not found.");
	}
	UserInfo userInfo = getUserInfo(con, user.getID());
	try {
		PropertyLoader.loadProperties();
		sendSMTP(
			PropertyLoader.getRequiredProperty(PropertyLoader.vcellSMTPHostName),
			new Integer(PropertyLoader.getRequiredProperty(PropertyLoader.vcellSMTPPort)).intValue(),
			PropertyLoader.getRequiredProperty(PropertyLoader.vcellSMTPEmailAddress),
			userInfo.email,
			"re: VCell Info",
			userInfo.password
		);
	} catch (Exception e) {
		e.printStackTrace();
		throw new DataAccessException("Error sending lost password\n"+e.getMessage(),e);
	}
	
}

public static void sendSMTP(String smtpHost, int smtpPort,String from, String to,String subject, String content)
	throws AddressException, MessagingException {
	
	// Create a mail session
	java.util.Properties props = new java.util.Properties();
	props.put("mail.smtp.host", smtpHost);
	props.put("mail.smtp.port", ""+smtpPort);
	Session session = Session.getDefaultInstance(props, null);
	
	// Construct the message
	Message msg = new MimeMessage(session);
	msg.setFrom(new InternetAddress(from));
	msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
	msg.setSubject(subject);
	msg.setText(content);
	
	// Send the message
	Transport.send(msg);
}

/**
 * getModel method comment.
 */
public UserInfo getUserInfo(Connection con, KeyValue key) throws SQLException, DataAccessException, ObjectNotFoundException {
	log.print("UserDbDriver.getUserInfo(key=" + key + ")");
	String sql;
	sql = 	" SELECT " + " * " + 
			" FROM " + userTable.getTableName() + 
			" WHERE " + userTable.id + " = " + key;

	//System.out.println(sql);

	//Connection con = conFact.getConnection();
	Statement stmt = con.createStatement();
	UserInfo userInfo = null;
	try {
		ResultSet rset = stmt.executeQuery(sql);

		//  	ResultSetMetaData metaData = rset.getMetaData();
		//  	for (int i=1;i<=metaData.getColumnCount();i++){
		//	  	System.out.println("column("+i+") = "+metaData.getColumnName(i));
		//  	}
		if (rset.next()){
			userInfo = userTable.getUserInfo(rset);
		}
	} finally {
		stmt.close(); // Release resources include resultset
	}
	if (userInfo == null) {
		throw new org.vcell.util.ObjectNotFoundException("UserInfo with id = '" + key + "' not found");
	}
	return userInfo;
}
/**
 * getModel method comment.
 */
public UserInfo[] getUserInfos(Connection con) throws SQLException, DataAccessException, ObjectNotFoundException {
	log.print("UserDbDriver.getUserInfos()");
	String sql;
	sql = " SELECT " + " * " + " FROM " + userTable.getTableName();

	//System.out.println(sql);

	//Connection con = conFact.getConnection();
	Statement stmt = con.createStatement();
	java.util.Vector<UserInfo> userList = null;
	try {
		ResultSet rset = stmt.executeQuery(sql);

		//  	ResultSetMetaData metaData = rset.getMetaData();
		//  	for (int i=1;i<=metaData.getColumnCount();i++){
		//	  	System.out.println("column("+i+") = "+metaData.getColumnName(i));
		//  	}
		userList = new java.util.Vector<UserInfo>();
		UserInfo userInfo;
		while (rset.next()){
			userInfo = userTable.getUserInfo(rset);
			userList.addElement(userInfo);
		}
	} finally {
		stmt.close(); // Release resources include resultset
	}
	if (userList.size() > 0) {
		UserInfo users[] = new UserInfo[userList.size()];
		userList.copyInto(users);
		return users;
	} else {
		return null;
	}
}
/**
 * addModel method comment.
 */
public KeyValue insertUserInfo(Connection con, UserInfo userInfo) throws SQLException {
	if (userInfo == null){
		throw new SQLException("Improper parameters for insertModel");
	}
	log.print("UserDbDriver.insertUserInfo(userinfo="+userInfo+")");
	//Connection con = conFact.getConnection();
	KeyValue key = DbDriver.getNewKey(con);
	insertUserInfoSQL(con,key,userInfo);
	return key;
}
/**
 * This method was created in VisualAge.
 * @param vcimage cbit.image.VCImage
 * @param userid java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
private void insertUserInfoSQL(Connection con, KeyValue key, UserInfo userInfo) throws SQLException {
	if (userInfo == null || con == null){
		throw new IllegalArgumentException("Improper parameters for insertUserSQL");
	}

	String sql;
	sql = "INSERT INTO " + userTable.getTableName() + " " +
			userTable.getSQLColumnList() + " VALUES " +
			userTable.getSQLValueList(key, userInfo);
	DbDriver.updateCleanSQL(con,sql);
	
	sql = "INSERT INTO " + UserStatTable.table.getTableName() + " " +
		UserStatTable.table.getSQLColumnList() + " VALUES " +
		UserStatTable.table.getSQLValueList(key);
	DbDriver.updateCleanSQL(con,sql);
}
/**
 * removeModel method comment.
 */
public void removeUserInfo(Connection con, User user) throws SQLException {
	if (user == null){
		throw new IllegalArgumentException("Improper parameters for removeUser");
	}
	log.print("UserDbDriver.removeUserInfo(user="+user+")");
	//Connection con = conFact.getConnection();
	removeUserInfoSQL(con,user);
}
/**
 * only the owner can delete a Model
 */
private void removeUserInfoSQL(Connection con, User user) throws SQLException {
	if (user == null){
		throw new IllegalArgumentException("Improper parameters for removeUserInfoSQL");
	}

	//Delete geomname row from GeomMainTable
	String sql;
	sql = "DELETE FROM " + userTable.getTableName() +
			" WHERE " + userTable.id+" = "+user.getID();
			
//System.out.println(sql);

	DbDriver.updateCleanSQL(con, sql);
}
/**
 * addModel method comment.
 */
public void updateUserInfo(Connection con, UserInfo userInfo) throws SQLException {
	if (userInfo == null){
		throw new SQLException("Improper parameters for insertModel");
	}
	log.print("UserDbDriver.updateUserInfo(userinfo="+userInfo+")");
	//Connection con = conFact.getConnection();
	updateUserInfoSQL(con,userInfo);
}
/**
 * This method was created in VisualAge.
 * @param vcimage cbit.image.VCImage
 * @param userid java.lang.String
 * @exception java.rmi.RemoteException The exception description.
 */
private void updateUserInfoSQL(Connection con, UserInfo userInfo) throws SQLException {
	if (userInfo == null || con == null){
		throw new IllegalArgumentException("Improper parameters for updateUserInfoSQL");
	}

	String sql;
	sql =	"UPDATE " + userTable.getTableName() +
			" SET "   + userTable.getSQLUpdateList(userInfo) + 
			" WHERE " + userTable.id     + " = " + userInfo.id;

//System.out.println(sql);
			
	DbDriver.updateCleanSQL(con,sql);
}

public void updateUserStat(Connection con, String userID) throws SQLException {
	if (userID == null){
		throw new SQLException("Improper parameters for insertModel");
	}
	User user = getUserFromUserid(con, userID);
	String sql;
	sql =	"UPDATE " + UserStatTable.table.getTableName() +
			" SET "   +
				UserStatTable.table.loginCount +" = "+
				UserStatTable.table.loginCount+" + 1"+","+
				UserStatTable.table.lastLogin + " = SYSDATE"+
			" WHERE " + UserStatTable.table.userRef + " = " + user.getID();
	DbDriver.updateCleanSQL(con,sql);

}

}
