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
    
    private String useOcpCertification;

    private final String SQLITE_DB_URL = "jdbc:sqlite:/var/lib/sqlite/data/persistency.db";

    private static Logger logger = Logger.getLogger(PersistSqlLiteDAO.class.getName());

    private PersistSqlLiteDAO() {
        ampAdminAddress = System.getenv("AMP_ADDRESS");
        accessToken = System.getenv("ACCESS_TOKEN");

        //TODO: for testing only, by pass the configuration step
        //ampAdminAddress = "amp-admin";
        //accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78";
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
    
    public String getUseOcpCertification() {
        if (useOcpCertification == null || useOcpCertification.equals("")) {
            readAmpConfiguration();
        }        
        return useOcpCertification;
    }

    public void setUseOcpCertification(String useOcpCertification) {
        this.useOcpCertification = useOcpCertification;
    }
    

    private void readAmpConfiguration() {
        Connection connection = null;
        Statement stmt = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            //TODO: later we need to add select configuration based on name, for now just retrieve one record.
            String sqlString = "SELECT * FROM CONFIGURATION_TABLE;";

            ResultSet rs = stmt.executeQuery(sqlString);

            while (rs.next()) {
                ampAdminAddress = rs.getString("admin_address");
                accessToken = rs.getString("access_token");
                accountId = rs.getString("account_id");
                useOcpCertification = rs.getString("use_ocp_certification");
                //logger.info("ampAdminAddress = " + ampAdminAddress);
                //logger.info("accessToken = " + accessToken);
                //logger.info("accountId = " + accountId);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void persistAmpConfiguration(String instanceId, String ampAdminAddress, String accessToken, String configurationName, String accountId, String useOcpCertification) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //connection.setAutoCommit(false);
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CONFIGURATION_TABLE (instance_id TEXT PRIMARY KEY, configuration_name TEXT,  admin_address TEXT, access_token TEXT, account_id TEXT, use_ocp_certification TEXT);");
            String sqlString = "insert into CONFIGURATION_TABLE values(\"" + instanceId + "\",\"" + configurationName + "\",\"" + ampAdminAddress + "\",\"" + accessToken + "\",\"" + accountId + "\",\"" + useOcpCertification+ "\");";
            logger.info("insert string: " + sqlString);
            stmt.executeUpdate(sqlString);
            //connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        //logger.info("persistAmpConfiguration: configurationName" + configurationName + " ampAdminAddress: " + ampAdminAddress);

    }

    public void deleteAmpConfiguration(String instanceId) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //connection.setAutoCommit(false);
            //logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlString = "delete from CONFIGURATION_TABLE where instance_id = \"" + instanceId + "\";";
            //logger.info("insert string: " + sqlString);
            stmt.executeUpdate(sqlString);
            //connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("deleteAmpConfiguration" + instanceId );

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

    public void persistProvisionInfo(String instanceId, Object provision) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //connection.setAutoCommit(false);
            //logger.info("persistProvisionInfo Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS PROVISION_TABLE (instance_id TEXT PRIMARY KEY,  provision_info TEXT);");
            String sqlString = "insert into PROVISION_TABLE values(\"" + instanceId + "\",\"" + provision.toString() + "\");";
            logger.info("insert string: " + sqlString);
            stmt.executeUpdate(sqlString);
            //connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("persistProvisionInfo: instanceId" + instanceId + " provision: " + provision.toString());

    }

    public void deleteProvisionInfo(String instanceId) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //connection.setAutoCommit(false);
            //logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlString = "delete from PROVISION_TABLE where instance_id =\"" + instanceId + "\";";
            //logger.info("sqlString: " + sqlString);
            stmt.executeUpdate(sqlString);
            //connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("deleteProvisionInfo: instanceId" + instanceId);

    }

    public String retrieveProvisionInfo(String instanceId) {
        Connection connection = null;
        Statement stmt = null;
        String result = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlString = "SELECT provision_info FROM PROVISION_TABLE where instance_id = \"" + instanceId + "\";";
            //logger.info("sqlString: " + sqlString);

            ResultSet rs = stmt.executeQuery(sqlString);

            while (rs.next()) {
                result = rs.getString("provision_info");
                //logger.info("provisionInfo = " + result);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        //logger.info("retrieveProvisionInfo: " + instanceId);
        return result;

    }

    public void persistBindingInfo(String instanceId, Object bindingInfo) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //connection.setAutoCommit(false);
            //logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS BINDING_TABLE (instance_id TEXT PRIMARY KEY,  binding_info TEXT);");
            String sqlString = "insert into BINDING_TABLE values(\"" + instanceId + "\",\"" + bindingInfo + "\");";
            //logger.info("sqlString: " + sqlString);
            stmt.executeUpdate(sqlString);
            //connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        //logger.info("persistBindingInfo: instanceId" + instanceId + " bindingInfo: " + bindingInfo);

    }
    
    public void deleteBindingInfo(String instanceId) {

        Connection connection = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //connection.setAutoCommit(false);
            //logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlString = "delete from BINDING_TABLE where instance_id= \"" + instanceId + "\";";
            logger.info("delete string: " + sqlString);
            stmt.executeUpdate(sqlString);
            //connection.commit();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        logger.info("deleteBindingInfo " + instanceId);

    }
    

    public Object retrieveBindingInfo(String instanceId) {
        Connection connection = null;
        Statement stmt = null;
        String result = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_DB_URL);
            //logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            String sqlString = "SELECT binding_info FROM BINDING_TABLE where instance_id = \"" + instanceId + "\";";
            //logger.info("select string: " + sqlString);

            ResultSet rs = stmt.executeQuery(sqlString);

            while (rs.next()) {
                result = rs.getString("binding_info");
                //logger.info("binding_info = " + result);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
        }
        //logger.info("retrieveBindingInfo: " + instanceId);
        return result;

    }
    
    
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
        
        dao.persistAmpConfiguration("instance1","http://test.com", "123456", "testConfiguration", "5","true");
        dao.persistAmpConfiguration("instance2","http://test1.com", "555555", "testConfiguration2", "6","false");
        System.out.println("--getAccessToken: " + dao.getAccessToken());
        System.out.println("--getAmpAdminAddress: " + dao.getAmpAdminAddress());
        System.out.println("--getAccountId: " + dao.getAccountId());
        
        dao.deleteProvisionInfo("instance1");
        System.out.println("--retrieveProvisionInfo: " + dao.retrieveProvisionInfo("instance1"));
        
        dao.deleteBindingInfo("instance1");
        System.out.println("--retrieveBindingInfo: " + dao.retrieveBindingInfo("instance1"));

        dao.deleteAmpConfiguration("instance2");
    }
    

}
