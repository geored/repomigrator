package org.geored.repomigrator.boundary.client.filters;

public class ErrorResponse {
    String errorMessage;
    String errorCode;
    String documentationLink;

    public ErrorResponse(String errorMessage, String errorCode, String documentationLink) {
        super();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.documentationLink = documentationLink;
    }

    public ErrorResponse() {

    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDocumentationLink() {
        return documentationLink;
    }

    public void setDocumentationLink(String documentationLink) {
        this.documentationLink = documentationLink;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}