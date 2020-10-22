package com.loadium.postman2jmx.model.postman;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostmanCollection {

    @JsonProperty("item")
    private List<PostmanItem> items = new ArrayList<>();

    @JsonProperty("info")
    private PostmanInfo info;

    @JsonProperty("variable")
    private List<PostmanVariable> vars = new ArrayList<>();

    public PostmanCollection() {
    }

    public PostmanCollection(List<PostmanItem> items, PostmanInfo info, List<PostmanVariable> vars) {
        this.items = items;
        this.info = info;
        this.vars = vars;
    }

    public List<PostmanItem> getItems() {
        return items;
    }

    public void setItems(List<PostmanItem> items) { this.items = items; }

    public PostmanInfo getInfo() {
        return info;
    }

    public void setInfo(PostmanInfo info) {
        this.info = info;
    }

    public List<PostmanVariable> getVars() {
        return vars;
    }

    public void setVars(List<PostmanVariable> vars) { this.vars = vars; }
}
