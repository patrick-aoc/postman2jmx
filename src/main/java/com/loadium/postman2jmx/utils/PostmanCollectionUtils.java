package com.loadium.postman2jmx.utils;

import com.loadium.postman2jmx.model.postman.PostmanCollection;
import com.loadium.postman2jmx.model.postman.PostmanItem;
import com.loadium.postman2jmx.model.postman.PostmanVariable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostmanCollectionUtils {

    private static void getItem(PostmanItem item, List<PostmanItem> itemList) {
        if (item.getItems().size() == 0 && item.getRequest() != null) {
            itemList.add(item);
        }

        for (PostmanItem i : item.getItems()) {
            getItem(i, itemList);
        }
    }

    public static List<PostmanItem> getItems(PostmanCollection postmanCollection) {
        List<PostmanItem> items = new ArrayList<>();

        for (PostmanItem item : postmanCollection.getItems()) {
            getItem(item, items);
        }
        return items;
    }

    private static void getVar(PostmanVariable var, List<PostmanVariable> vars) {
        if (var.getVars().size() == 0 && var.getKey() != null && var.getValue() != null) {
            vars.add(var);
        }

        for (PostmanVariable i : var.getVars()) {
            getVar(i, vars);
        }
    }

    public static List<PostmanVariable> getVars(PostmanCollection postmanCollection) {
        List<PostmanVariable> vars = new ArrayList<>();

        for (PostmanVariable var : postmanCollection.getVars()) {
            getVar(var, vars);
        }
        return vars;
    }
}
