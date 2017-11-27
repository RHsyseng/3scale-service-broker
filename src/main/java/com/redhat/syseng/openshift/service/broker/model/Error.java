package com.redhat.syseng.openshift.service.broker.model;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Error {

    private int code;

    private String description;

    private String details;

    @SuppressWarnings("unused")
    private Error() {
    }

    public Error(int code, String description, Throwable throwable) {
        this.code = code;
        if (description != null) {
            this.description = description;
        } else if (throwable != null) {
            this.description = throwable.getMessage();
        }
        if (throwable != null) {
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            this.details = writer.toString();
        }
    }

    public Error(int code, String description) {
        this(code, description, null);
    }

    public Error(int code, Throwable throwable) {
        this(code, null, throwable);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code=" + code +
                ", description='" + description + '\'' +
                ", details='" + details + '\'' +
                '}';
    }

    public WebApplicationException asException() {
        ResponseBuilder responseBuilder = Response.status(code);
        responseBuilder = responseBuilder.entity(this);
        return new WebApplicationException(responseBuilder.build());
    }
}
