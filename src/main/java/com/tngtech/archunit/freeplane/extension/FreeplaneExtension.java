/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.util.Properties;

import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.extension.ArchUnitExtension;
import com.tngtech.archunit.lang.extension.EvaluatedRule;

public class FreeplaneExtension implements ArchUnitExtension {
    public static final String UNIQUE_IDENTIFIER = "freeplane-archunit-extension";
    private FreeplaneClient freeplaneClient;

    FreeplaneExtension(FreeplaneClient freeplaneClient) {
        this.freeplaneClient = freeplaneClient;
    }

    @SuppressWarnings("unused")
    public FreeplaneExtension() {
        this(null);
    }

    @Override
    public String getUniqueIdentifier() {
        return UNIQUE_IDENTIFIER;
    }

    @Override
    public void configure(Properties properties) {
        final String portSpec = properties.getProperty("port", "6297");
        int port = Integer.parseInt(portSpec);
        freeplaneClient = new FreeplaneClient("localhost", port);
    }

    @Override
    public void handle(EvaluatedRule evaluatedRule) {
        final EvaluationResult result = evaluatedRule.getResult();
        if(! result.hasViolation())
            return;
        TransferObjectBuilder transferObjectBuilder = new TransferObjectBuilder();
        result.handleViolations(transferObjectBuilder::handle);

        ArchitectureViolations architectureViolations = transferObjectBuilder.buildTransferObject(evaluatedRule.getRule().getDescription());
        if(! freeplaneClient.sendJson(architectureViolations))
            ArchConfiguration.get().configureExtension(FreeplaneExtension.UNIQUE_IDENTIFIER)
                .setProperty("enabled", false);
    }

}