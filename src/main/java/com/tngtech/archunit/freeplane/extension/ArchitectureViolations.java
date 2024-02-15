/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArchitectureViolations {
    final public String violatedRuleDescription;
    final public Map<String, Set<String>> violatingClassLocations;
    final public List<ViolationDescription> violationDescriptions;
    final public boolean isNoCyclesConditionChecked;
    public ArchitectureViolations(String violatedRuleDescription,
            Map<String, Set<String>> violatingClassLocations,
            List<ViolationDescription> violationDescriptions,
            boolean isNoCyclesConditionChecked) {
        super();
        this.violatedRuleDescription = violatedRuleDescription;
        this.violatingClassLocations = violatingClassLocations;
        this.violationDescriptions = violationDescriptions;
        this.isNoCyclesConditionChecked = isNoCyclesConditionChecked;
    }
    @Override
    public String toString() {
        return "ArchTestResult [violatedRuleDescription=" + violatedRuleDescription
                + ", violatingClassLocations=" + violatingClassLocations
                + ", violationDescriptions=" + violationDescriptions
                + ", isNoCyclesConditionChecked=" + isNoCyclesConditionChecked + "]";
    }
}
