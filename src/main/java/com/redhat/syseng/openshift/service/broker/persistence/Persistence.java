package com.redhat.syseng.openshift.service.broker.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Persistence {

    private static final String SQLITE_DB_URL = "jdbc:sqlite:/var/lib/sqlite/data/persistency.db";

    private static final Logger logger = Logger.getLogger(Persistence.class.getName());

    private static Persistence INSTANCE;

    private PlatformConfig platformConfig;

    private Boolean loadSecuredMarket;

    private boolean persistenceLoaded = false;

    private Persistence() {
        String loadSecuredMarketString = System.getenv("LOAD_SECURED_SERVICE_MARKET");
        if (loadSecuredMarketString == null) {
            loadSecuredMarket = Boolean.FALSE;
        } else {
            loadSecuredMarket = Boolean.valueOf(loadSecuredMarketString);
        }
    }

    public static Persistence getInstance() {
        if (INSTANCE == null) {
            synchronized (Persistence.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Persistence();
                }
            }
        }
        return INSTANCE;
    }

    public PlatformConfig getPlatformConfig() {
        logger.info("getPlatformConfig " + persistenceLoaded);
        if (!persistenceLoaded) {
            platformConfig = readPlatformConfig();
        }
        return platformConfig;
    }

    private PlatformConfig readPlatformConfig() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load SQLite driver");
        }
        try (Connection connection = DriverManager.getConnection(SQLITE_DB_URL)) {
            Statement stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            //TODO: later, we might need to select configuration based on name, for now just retrieve one record.
            String sqlString = "SELECT * FROM CONFIGURATION_TABLE;";
            ResultSet rs = stmt.executeQuery(sqlString);

            if (rs.next()) {
                PlatformConfig platformConfig = new PlatformConfig();
                platformConfig.setAdminAddress(rs.getString("admin_address"));
                platformConfig.setAccessToken(rs.getString("access_token"));
                platformConfig.setAccountId(rs.getString("account_id"));
                logger.info("rs.getString(\"use_ocp_certification\") " + rs.getString("use_ocp_certification"));                
                logger.info("rs.getBoolean(\"use_ocp_certification\") " + rs.getBoolean("use_ocp_certification"));                
                platformConfig.setUseOcpCertificate(rs.getBoolean("use_ocp_certification"));
                logger.info("Loaded " + platformConfig);
                persistenceLoaded = true;                
                return platformConfig;
            } else {
                logger.info("no configuration to load, this is the initial stage ");
                return null;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("no such table: CONFIGURATION_TABLE")) {
                //This is normal, because 1st time read the sqlite3, the table is not even created. 
                logger.info("no such table: CONFIGURATION_TABLE, and configuration to load, this is the initial stage ");
                return null;
            } else {
                throw new IllegalStateException("Failed to read configuration from SQLite: " + e);
            }
        }
    }

    public void setConfiguration(String instanceId, String configurationName, PlatformConfig platformConfig) throws ClassNotFoundException, SQLException {

        Statement stmt = null;
        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection(SQLITE_DB_URL)) {
            logger.info("Opened database successfully");

            stmt = connection.createStatement();
            stmt.setQueryTimeout(30);  // set timeout to 30 sec.

            //Boolean Datatype. SQLite does not have a separate Boolean storage class. Instead, Boolean values are stored as integers 0 (false) and 1 (true).
            //without conversion, Java boolean true will be stored as "true", then when retrive using rs.getBoolean, the value will be false.
            int booleanValueInSqlite = 1;
            if (!platformConfig.isUseOcpCertificate()) booleanValueInSqlite = 0;

            
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CONFIGURATION_TABLE (instance_id TEXT PRIMARY KEY, configuration_name TEXT,  admin_address TEXT, access_token TEXT, account_id TEXT, use_ocp_certification TEXT);");
            String sqlString = "insert into CONFIGURATION_TABLE values(\"" + instanceId + "\",\"" + configurationName + "\",\"" + platformConfig.getAdminAddress() + "\",\"" + platformConfig.getAccessToken() + "\",\"" + platformConfig.getAccountId() + "\",\"" + booleanValueInSqlite + "\");";
            logger.info("insert string: " + sqlString);
            stmt.executeUpdate(sqlString);
        }
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
        logger.info("deleteAmpConfiguration" + instanceId);

    }

    public boolean isLoadSecuredMarket() {
        return loadSecuredMarket;
    }

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

    public boolean isProvisionInfoExist(String instanceId) {

        Connection connection = null;
        Statement stmt = null;
        boolean result = false;
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
                result = true;
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch (Exception e) {
            logger.info(e.getClass().getName() + ": " + e.getMessage());
            /*no need to do the judge below, since if the table is not there, of course the info doesn't exist
            if (e.getMessage().contains("no such table: PROVISION_TABLE")) {
                //This is normal, because 1st time read the sqlite3, the table is not even created. 
                logger.info("no such table: PROVISION_TABLE, this is the initial stage ");
                */
        }
        //logger.info("isProvisionInfoExist: " + result);
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
}
