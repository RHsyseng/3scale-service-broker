package com.redhat.syseng.openshift.service.broker.service.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

public class RestClientRequestFilter implements ClientRequestFilter {
    //value from pod: 
    //cat /var/run/secrets/kubernetes.io/serviceaccount/token
    String value = "Bearer" + "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiIzc2NhbGUtc2VydmljZS1icm9rZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiZGVmYXVsdC10b2tlbi1qNWxxaCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiNDc4NzRmYWEtZGEwMS0xMWU3LWIzYWEtYmVlZmZlZWQwMDVkIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OjNzY2FsZS1zZXJ2aWNlLWJyb2tlcjpkZWZhdWx0In0.OHaYLt09WzrKcz-OLgriOlBzruBV7njMflpzDizrFqDhvqpD8mqjmOonXQ_gblE-wfVQiWM0TnZ1OrHvs6FoiHkUStm8MxNO7lriTTzptlMBDIfVz5gwISPyhVaY5W5nK8i4qEwzHjo2-XSSxDjuEprdyNt-f6R7KFPxeFFoD3C98qcd2mYWBbNKOida8vGi9QhJUcrKRZQGpn82ix2HhOrhfReLHTyqH1Nt40icaAtB26VUL4lGWGMTxt7__uSM1k2q8989AMlcMoLtOAjkT0yHNutHDEirC6xsALmoX_oayRZT4eLLNQWoLpvYxysF2Gqbv7Eo7zMGRT8OyMSQRQ";

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> mmap = requestContext.getHeaders();
        List<Object> list = new ArrayList<Object>();
        list.add(value);
        mmap.put("Authorization", list);
    }
}
