package org.geored.repomigrator.entity;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ValidatedArtifactStore implements Serializable {

    boolean valid;
    String repositoryUrl;

    String storeKey;
    Map<String,String> errors;


    ValidatedArtifactStore() {
    }


    public static class Builder {

        boolean valid;
        String repositoryUrl;

        String storeKey;
        Map<String,String> errors;

        public Builder(String storeKey) {

            this.storeKey = storeKey;
            this.errors = new HashMap<>();
            this.valid = false;
        }


        public Builder setRepositoryUrl(String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
            return this;
        }

        public Builder setValid(Boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder setErrors(Map<String,String> errors) {
            this.errors = errors;
            return this;
        }

        public ValidatedArtifactStore build() {
            ValidatedArtifactStore artifactStoreValidateData = new ValidatedArtifactStore();
            artifactStoreValidateData.valid = this.valid;
            artifactStoreValidateData.repositoryUrl = this.repositoryUrl;
            artifactStoreValidateData.errors = this.errors;
            artifactStoreValidateData.storeKey = this.storeKey;
            return artifactStoreValidateData;
        }
    }

    public boolean isValid() {
        return valid;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getStoreKey() {
        return storeKey;
    }

    public void setStoreKey(String storeKey) {
        this.storeKey = storeKey;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
