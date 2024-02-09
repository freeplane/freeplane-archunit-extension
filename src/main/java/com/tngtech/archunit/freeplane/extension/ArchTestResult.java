/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArchTestResult {
    final public String violatedRuleDescription;
    final public Map<String, Set<String>> violatingClassLocations;
    final public List<String> violationDescriptions;
    final public Set<String> violationDependencyDescriptions;
    public ArchTestResult(String violatedRuleDescription,
            Map<String, Set<String>> violatingClassLocations,
            List<String> violationDescriptions,
            Set<String> violationDependencyDescriptions) {
        super();
        this.violatedRuleDescription = violatedRuleDescription;
        this.violatingClassLocations = violatingClassLocations;
        this.violationDescriptions = violationDescriptions;
        this.violationDependencyDescriptions = violationDependencyDescriptions;
    }
    @Override
    public String toString() {
        return "ArchTestResult ["
                + "violatedRuleDescription=" + violatedRuleDescription + "\n"
                + "violatingClassLocations=" + violatingClassLocations + "\n"
                + "violationDescriptions=" + violationDescriptions + "\n"
                + "violationDependencyDescriptions=" + violationDependencyDescriptions + "]";
    }

}
