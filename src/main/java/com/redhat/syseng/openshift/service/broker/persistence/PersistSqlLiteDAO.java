/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.persistence;

import java.sql.*;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author czhu
 */
public class PersistSqlLiteDAO {

    private HashMap map = new HashMap();

    private static PersistSqlLiteDAO INSTANCE;

    private String accessToken;

    private String ampAdminAddress;

    private String accountId;

    private Boolean loadSecuredMarket;

    private final String SQLITE_DB_URL = "jdbc:sqlite:/tmp/persistency.db";

    private static Logger logger = Logger.getLogger(PersistSqlLiteDAO.class.getName());

    private PersistSqlLiteDAO() {
        ampAdminAddress = System.getenv("AMP_ADDRESS");
        accessToken = System.getenv("ACCESS_TOKEN");

        //TODO: for testing only, by pass the configuration step
        //ampAdminAddress = "amp-admin";
        //accessToken = "34bbf459e196ac29e20aac4eace64f83445e7ea5f0c129f6d91e67d5666388c3";
        String loadSecuredMarketString = System.getenv("LOAD_SECURED_SERVICE_MARKET");
        if (loadSecuredMarketString != null) {
            loadSecuredMarket = Boolean.valueOf(loadSecuredMarketString);
        }
    }

    public static PersistSqlLiteDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (PersistSqlLiteDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PersistSqlLiteDAO();
                }
            }
        }
        return INSTANCE;
    }

    public String getAccessToken() {
        if (accessToken == null || accessToken.equals("")) {
            readAmpConfiguration();
        }
        return accessToken;
    }

    public String getAmpAdminAddress() {
        if (ampAdminAddress == null || ampAdminAddress.equals("")) {
            readAmpConfiguration();
        }
        return ampAdminAddress;
    }

    public String getAccountId() {
        if (accountId == null || accountId.equals("")) {
            readAmpConfiguration();
        }
        return accountId;
    }

    private void readAmpConfiguration() {
        Connection connection = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            //TODO: later we need to add select configuration based on name, for now just retrieve one record.
            String sqlString = "SELECT * FROM CONFIGURATION_TABLE;";

            ResultSet rs = stmt.executeQuery(sqlString);

            while (rs.next()) {
                ampAdminAddress = rs.getString("admin_address");
                accessToken = rs.getString("access_token");
                accountId = rs.getString("account_id");
                logger.info("ampAdminAddress = " + ampAdminAddress);
                logger.info("accessToken = " + accessToken);
                logger.info("accountId = " + accountId);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void persistAmpConfiguration(String ampAdminAddress, String accessToken, String configurationName, String accountId) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            connection.setAutoCommit(false);
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CONFIGURATION_TABLE (configuration_name TEXT PRIMARY KEY,  admin_address TEXT, access_token TEXT, account_id TEXT);");
            String sqlString = "insert into CONFIGURATION_TABLE values(\"" + configurationName + "\",\"" + ampAdminAddress + "\",\"" + accessToken + "\",\"" + accountId + "\");";
            logger.info("insert string: " + sqlString);
            stmt.executeUpdate(sqlString);
            connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("persistAmpConfiguration: configurationName" + configurationName + " ampAdminAddress: " + ampAdminAddress);

    }

    public boolean isLoadSecuredMarket() {
        if (loadSecuredMarket == null) {
            return true;
        }
        return loadSecuredMarket;
    }

    public void setLoadSecuredMarket(boolean loadSecuredMarket) {
        this.loadSecuredMarket = loadSecuredMarket;
    }

    /*
    test PreparedStatement

    public static void main(String args[]) {

        Connection connection = null;
        Statement stmt = null;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:/home/czhu/works/sqlite3/persistency.db");
            connection.setAutoCommit(false);

            System.out.println("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS provision (instance_id TEXT PRIMARY KEY,  provision_info TEXT);");
            //stmt.executeUpdate("insert into provision values(1, 'hello');");
            //stmt.executeUpdate("insert into provision values(2, 'calvin');");

            //String createTable = "CREATE TABLE IF NOT EXISTS provision (instance_id TEXT,  provision_info TEXT);";
            String updateSql = "insert into provision values(?, ?);";
            preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, "what");
            preparedStatement.executeUpdate();
            
            preparedStatement.setString(1, "2");
            preparedStatement.setString(2, "where");
            preparedStatement.executeUpdate();


            ResultSet rs = stmt.executeQuery("SELECT * FROM provision;");

            while (rs.next()) {
                int id = rs.getInt("instance_id");
                String provisionInfo = rs.getString("provision_info");

                System.out.println("ID = " + id);
                System.out.println("NAME = " + provisionInfo);
                System.out.println();
            }
            
            connection.commit();
            rs.close();
            stmt.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }

    public static void main(String args[]) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:/home/czhu/works/sqlite3/persistency.db");
            connection.setAutoCommit(false);

            System.out.println("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS provision (instance_id TEXT PRIMARY KEY,  provision_info TEXT);");
            stmt.executeUpdate("insert into provision values(1, 'hello');");
            stmt.executeUpdate("insert into provision values(2, 'calvin');");

            ResultSet rs = stmt.executeQuery("SELECT * FROM provision;");

            while (rs.next()) {
                int id = rs.getInt("instance_id");
                String provisionInfo = rs.getString("provision_info");

                System.out.println("instance_id = " + id);
                System.out.println("provision_info = " + provisionInfo);
                System.out.println();
            }

            connection.commit();

            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
    }
     */
    public static void main(String args[]) {
        PersistSqlLiteDAO dao = new PersistSqlLiteDAO();
        dao.persistProvisionInfo("instance1", "provision1");
        dao.persistProvisionInfo("instance2", "provision2");
        System.out.println("--retrieveProvisionInfo: " + dao.retrieveProvisionInfo("instance1"));
        System.out.println();

        dao.persistBindingInfo("instance1", "binding1");
        dao.persistBindingInfo("instance2", "binding2");
        System.out.println("--retrieveBindingInfo: " + dao.retrieveBindingInfo("instance1"));
        System.out.println();
        
        dao.persistAmpConfiguration("http://test.com", "123456", "testConfiguration", "5");
        dao.persistAmpConfiguration("http://test1.com", "555555", "testConfiguration2", "6");
        System.out.println("--getAccessToken: " + dao.getAccessToken());
        System.out.println("--getAmpAdminAddress: " + dao.getAmpAdminAddress());
        System.out.println("--getAccountId: " + dao.getAccountId());

    }

    public void persistProvisionInfo(String instanceID, Object provision) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            connection.setAutoCommit(false);
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS PROVISION_TABLE (instance_id TEXT PRIMARY KEY,  provision_info TEXT);");
            String sqlString = "insert into PROVISION_TABLE values(\"" + instanceID + "\",\"" + provision.toString() + "\");";
            logger.info("insert string: " + sqlString);
            stmt.executeUpdate(sqlString);
            connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("persistProvisionInfo: instanceID" + instanceID + " provision: " + provision.toString());

    }

    public Object retrieveProvisionInfo(String instanceID) {
        Connection connection = null;
        Statement stmt = null;
        String result = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlString = "SELECT provision_info FROM PROVISION_TABLE where instance_id = \"" + instanceID + "\";";
            logger.info("select string: " + sqlString);

            ResultSet rs = stmt.executeQuery(sqlString);

            while (rs.next()) {
                result = rs.getString("provision_info");
                logger.info("provisionInfo = " + result);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("retrieveProvisionInfo: " + instanceID);
        return result;

    }

    public void persistBindingInfo(String instanceID, Object bindingInfo) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            connection.setAutoCommit(false);
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS BINDING_TABLE (instance_id TEXT PRIMARY KEY,  binding_info TEXT);");
            String sqlString = "insert into BINDING_TABLE values(\"" + instanceID + "\",\"" + bindingInfo + "\");";
            logger.info("insert string: " + sqlString);
            stmt.executeUpdate(sqlString);
            connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("persistBindingInfo: instanceID" + instanceID + " bindingInfo: " + bindingInfo);

    }

    public Object retrieveBindingInfo(String instanceID) {
        Connection connection = null;
        Statement stmt = null;
        String result = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlString = "SELECT binding_info FROM BINDING_TABLE where instance_id = \"" + instanceID + "\";";
            logger.info("select string: " + sqlString);

            ResultSet rs = stmt.executeQuery(sqlString);

            while (rs.next()) {
                result = rs.getString("binding_info");
                logger.info("binding_info = " + result);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("retrieveBindingInfo: " + instanceID);
        return result;

    }

}
