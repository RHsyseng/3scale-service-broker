package com.redhat.syseng.openshift.service.broker.persistence;

public class PlatformConfig {
    private String accessToken;
    private String adminAddress;
    private String accountId;
    private boolean useOcpCertificate;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAdminAddress() {
        return adminAddress;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isUseOcpCertificate() {
        return useOcpCertificate;
    }

    public void setUseOcpCertificate(boolean useOcpCertificate) {
        this.useOcpCertificate = useOcpCertificate;
    }

    @Override
    public String toString() {
        return "PlatformConfig{" +
                "accessToken='" + accessToken + '\'' +
                ", adminAddress='" + adminAddress + '\'' +
                ", accountId='" + accountId + '\'' +
                ", useOcpCertificate=" + useOcpCertificate +
                '}';
    }
}
