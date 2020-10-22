package com.loadium.postman2jmx.builder;

import com.loadium.postman2jmx.config.Postman2JmxConfig;
import com.loadium.postman2jmx.exception.NoPostmanCollectionItemException;
import com.loadium.postman2jmx.exception.NullPostmanCollectionException;
import com.loadium.postman2jmx.model.jmx.*;
import com.loadium.postman2jmx.model.postman.PostmanCollection;
import com.loadium.postman2jmx.model.postman.PostmanItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractJmxFileBuilder implements IJmxFileBuilder {

    protected JmxFile buildJmxFile(PostmanCollection postmanCollection, String jmxOutputFilePath) throws Exception {
        if (postmanCollection == null) {
            throw new NullPostmanCollectionException();
        }

        if (postmanCollection.getItems() == null || postmanCollection.getItems().isEmpty()) {
            throw new NoPostmanCollectionItemException();
        }

        Postman2JmxConfig config = new Postman2JmxConfig();
        config.setJMeterHome();

        // TestPlan
        // TestPlan testPlan = JmxTestPlan.newInstance(postmanCollection.getInfo() != null ? postmanCollection.getInfo().getName() : "");
        TestPlan testPlan = JmxTestPlan.newInstance(postmanCollection.getInfo() != null ? postmanCollection.getInfo().getName() : "", postmanCollection.getVars());

        // ThreadGroup controller
        LoopController loopController = JmxLoopController.newInstance();

        // ThreadGroup
        ThreadGroup threadGroup = JmxThreadGroup.newInstance(loopController);

        // HTTPSamplerProxy
        List<HTTPSamplerProxy> httpSamplerProxies = new ArrayList<>();
        List<HeaderManager> headerManagers = new ArrayList<>();
        List<JSONPostProcessor> jsonExtractors = new ArrayList<>();

        for (PostmanItem item : postmanCollection.getItems()) {
          /*  if (!item.getEvent().isEmpty()) {
                continue;
            }*/

            IJmxBodyBuilder bodyBuilder = JmxBodyBuilderFactory.getJmxBodyBuilder(item);
            HTTPSamplerProxy httpSamplerProxy = bodyBuilder.buildJmxBody(item);
            httpSamplerProxies.add(httpSamplerProxy);

            headerManagers.add(JmxHeaderManager.newInstance(item.getName(), item.getRequest().getHeaders()));

            // Retrieve the script element for the corresponding request
            List<String> postTestScript = scriptParser(item.getEvents().get(0).getScript().getExec());
            String varNames = postTestScript.get(0);
            String varValues = postTestScript.get(1);

            JSONPostProcessor postProcessor = null;

            // Remove the excess comma at the end of the strings.
            if(!varNames.isEmpty() && !varValues.isEmpty()) {
                postProcessor = JmxJsonPostProcessor.
                        newInstance(StringUtils.substring(varNames, 0, varNames.length() - 1),
                                    StringUtils.substring(varValues, 0, varValues.length() - 1));
            }

            jsonExtractors.add(postProcessor);
        }

        // Create TestPlan hash tree
        HashTree testPlanHashTree = new ListedHashTree();
        testPlanHashTree.add(testPlan);

        // Add ThreadGroup to TestPlan hash tree
        HashTree threadGroupHashTree = new ListedHashTree();
        threadGroupHashTree = testPlanHashTree.add(testPlan, threadGroup);

        // Add Http Cookie Manager
        threadGroupHashTree.add(JmxCookieManager.newInstance());

        // Add Http Sampler to ThreadGroup hash tree
        HashTree httpSamplerHashTree = new ListedHashTree();

        // Add header manager hash tree
        HashTree headerHashTree = null;

        // Add Java Sampler to ThreadGroup hash tree
        for (int i = 0; i < httpSamplerProxies.size(); i++) {
            HTTPSamplerProxy httpSamplerProxy = httpSamplerProxies.get(i);
            HeaderManager headerManager = headerManagers.get(i);
            JSONPostProcessor jsonPostProcessor = jsonExtractors.get(i);

            httpSamplerHashTree = threadGroupHashTree.add(httpSamplerProxy);

            headerHashTree = new HashTree();
            headerHashTree = httpSamplerHashTree.add(headerManager);

            PostmanItem postmanItem = postmanCollection.getItems().get(i);
            if (!postmanItem.getEvents().isEmpty()) {
                List<JSONPostProcessor> jsonPostProcessors = JmxJsonPostProcessor.getJsonPostProcessors(postmanItem);
                httpSamplerHashTree.add(jsonPostProcessors);
            }

            /* We only need a JSON post processor if there are dynamic variables
               from any of the test scripts. */
            if(jsonPostProcessor != null) {
                httpSamplerHashTree.add(jsonPostProcessor);
            }
        }

        File file = new File(jmxOutputFilePath);
        OutputStream os = new FileOutputStream(file);
        SaveService.saveTree(testPlanHashTree, os);

        InputStream is = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        for (int len = 0; (len = is.read(buffer)) != -1; ) {
            bos.write(buffer, 0, len);
        }
        bos.flush();

        byte[] data = bos.toByteArray();
        JmxFile jmxFile = new JmxFile(data, testPlanHashTree);

        os.close();
        is.close();
        bos.close();

        return jmxFile;
    }

    private List<String> scriptParser(List<String> script) {
        String varNames = "";
        String varValues = "";

        List<String> varNV = new ArrayList<>();

        for(String line : script) {

            /* We make a check to see if the script is trying to set
               a variable dynamically */
            boolean hasVarDefn = line.contains(".set");

            if (hasVarDefn) {

                /* The variable name has quotes around it, so split the
                   line of code at the quote */
                List<String> fragments = new ArrayList<>();
                String[] codeLine = line.split("\"");

                String varName = codeLine[1];
                String varValue = "$.";

                String[] values = codeLine[2].split("\\.");

                for(int i = 0; i < values.length; i++) {
                    if (i != 0) {
                        varValue = varValue + values[i];

                        if (i + 1 < values.length) {
                            varValue = varValue + ".";
                        }
                    }
                }

                /* The string will have ");" at the end, so we need to make sure to
                   rmove it. */
                varValue = StringUtils.substring(varValue, 0, varValue.length() - 2);

                varNames = varNames + varName + ",";
                varValues = varValues + varValue + ",";
            }
        }

        varNV.add(varNames);
        varNV.add(varValues);

        return varNV;
    }
}
