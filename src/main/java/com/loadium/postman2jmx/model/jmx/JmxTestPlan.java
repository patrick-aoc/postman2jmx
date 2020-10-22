package com.loadium.postman2jmx.model.jmx;

import com.loadium.postman2jmx.model.postman.PostmanItem;
import com.loadium.postman2jmx.model.postman.PostmanVariable;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;

import java.util.List;

public class JmxTestPlan {

    public static TestPlan newInstance(String name) {
        TestPlan testPlan = new TestPlan();
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setName(name);
        testPlan.setEnabled(true);
        testPlan.setFunctionalMode(false);
        testPlan.setComment("");
        testPlan.setSerialized(false);
        testPlan.setTestPlanClasspath("");
        testPlan.setUserDefinedVariables(new Arguments());
        return testPlan;
    }

    public static TestPlan newInstance(String name, List<PostmanVariable> postmanVariables) {
        TestPlan testPlan = new TestPlan();
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setName(name);
        testPlan.setEnabled(true);
        testPlan.setFunctionalMode(false);
        testPlan.setComment("");
        testPlan.setSerialized(false);
        testPlan.setTestPlanClasspath("");

        Arguments args = defineVariables(testPlan, postmanVariables);
        testPlan.setUserDefinedVariables(args);
        // testPlan.setUserDefinedVariables(new Arguments());
        return testPlan;
    }

    public static Arguments defineVariables(TestPlan testPlan, List<PostmanVariable> postmanVariables) {
        Arguments args = new Arguments();

        if (postmanVariables != null && postmanVariables.size() != 0) {
            for(PostmanVariable var : postmanVariables) {
                if(!var.getValue().isEmpty()) {
                    args.addArgument(var.getKey(), var.getValue());
                }
            }

        }
        return args;
    }
}
