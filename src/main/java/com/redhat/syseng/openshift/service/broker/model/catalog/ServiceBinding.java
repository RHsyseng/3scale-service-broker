/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class ServiceBinding {
    private Create create;

    public Create getCreate() {
        return create;
    }

    public void setCreate(Create create) {
        this.create = create;
    }

    @Override
    public String toString() {
        return "ServiceBinding{" +
                "create=" + create +
                '}';
    }
}
