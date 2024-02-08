/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class EvaluatedRuleDto {
    final public String violatedRuleDescription;
    final public Set<String> violatingClassLocations;
    final public List<String> violationDescriptions;
    final public Set<String> violationDependencyDescriptions;
    public EvaluatedRuleDto(String violatedRuleDescription,
            SortedSet<String> violatingClassLocations,
            List<String> violationDescriptions,
            SortedSet<String> violationDependencyDescriptions) {
        super();
        this.violatedRuleDescription = violatedRuleDescription;
        this.violatingClassLocations = violatingClassLocations;
        this.violationDescriptions = violationDescriptions;
        this.violationDependencyDescriptions = violationDependencyDescriptions;
    }
    @Override
    public String toString() {
        return "EvaluatedRuleDto ["
                + "violatedRuleDescription=" + violatedRuleDescription + "\n"
                + "violatingClassLocations=" + violatingClassLocations + "\n"
                + "violationDescriptions=" + violationDescriptions + "\n"
                + "violationDependencyDescriptions=" + violationDependencyDescriptions + "]";
    }

}
