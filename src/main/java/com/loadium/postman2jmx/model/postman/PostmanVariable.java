package com.loadium.postman2jmx.model.postman;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostmanVariable {

    @JsonProperty("variable")
    private List<PostmanVariable> vars = new ArrayList<>();

    @JsonProperty("key")
    private String key;

    @JsonProperty("value")
    private String value;

    public PostmanVariable() {
    }

    public PostmanVariable(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(String key) { this.key = key; }

    public String getKey() { return this.key; }

    public void setValue(String value) { this.value = value; }

    public String getValue() { return this.value; }

    public List<PostmanVariable> getVars() {
        return this.vars;
    }

    public void setVars(List<PostmanVariable> vars) {
        this.vars = vars;
    }
}
