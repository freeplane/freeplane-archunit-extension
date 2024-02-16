/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArchitectureViolations {
    final private String violatedRuleDescription;
    private Map<String, Set<String>> violatingClassLocations;
    private List<ViolationDescription> violationDescriptions;
    public ArchitectureViolations(String violatedRuleDescription,
            Map<String, Set<String>> violatingClassLocations,
            List<ViolationDescription> violationDescriptions) {
        super();
        this.violatedRuleDescription = violatedRuleDescription;
        this.violatingClassLocations = violatingClassLocations;
        this.violationDescriptions = violationDescriptions;
    }



    public String getViolatedRuleDescription() {
        return violatedRuleDescription;
    }



    public Map<String, Set<String>> getViolatingClassLocations() {
        if(violatingClassLocations == null)
            violatingClassLocations = Collections.emptyMap();
        return violatingClassLocations;
    }



    public List<ViolationDescription> getViolationDescriptions() {
        if(violationDescriptions == null)
            violationDescriptions = Collections.emptyList();
        return violationDescriptions;
    }



    @Override
    public String toString() {
        return "ArchTestResult [violatedRuleDescription=" + violatedRuleDescription
                + ", violatingClassLocations=" + violatingClassLocations
                + ", violationDescriptions=" + violationDescriptions + "]";
    }
}
