/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.persistence;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author czhu
 */
public class PersistHashMapDAO {

    private HashMap map = new HashMap();

    private static PersistHashMapDAO INSTANCE;

    private String accessToken;

    private String ampAdminAddress;

    private Boolean loadSecuredMarket;

    private static Logger logger = Logger.getLogger(PersistHashMapDAO.class.getName());

    private PersistHashMapDAO() {
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

    public static PersistHashMapDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (PersistHashMapDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PersistHashMapDAO();
                }
            }
        }
        return INSTANCE;
    }

    public void persistProvisionInfo(String instanceID, Object provision) {
        map.put(instanceID, provision);
        logger.info("PersistDao: instanceID" + instanceID + " provision: " + provision.toString());
    }

    public Object retrieveProvisionInfo(String instanceID) {
        return map.get(instanceID);

    }

    public void persistBindingInfo(String instanceID, String bindingInfo) {
        map.put(instanceID, bindingInfo);
        logger.info("PersistDao: instanceID" + instanceID + " bindingInfo: " + bindingInfo);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAmpAdminAddress() {
        return ampAdminAddress;
    }

    public void setAmpAdminAddress(String ampAdminAddress) {
        this.ampAdminAddress = ampAdminAddress;
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
}
