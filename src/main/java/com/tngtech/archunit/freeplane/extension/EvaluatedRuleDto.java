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
    final public Set<String> importedClassUris;
    final public String violatedRuleDescription;
    final public List<String> violationDescriptions;
    final public Set<String> violatingClasses;
    final public Set<String> violationDependencyDescriptions;
    public EvaluatedRuleDto(SortedSet<String> importedClassUris, String violatedRuleDescription,
            List<String> violationDescriptions,
            SortedSet<String> violatingClasses,
            SortedSet<String> violationDependencyDescriptions) {
        super();
        this.importedClassUris = importedClassUris;
        this.violatedRuleDescription = violatedRuleDescription;
        this.violationDescriptions = violationDescriptions;
        this.violatingClasses = violatingClasses;
        this.violationDependencyDescriptions = violationDependencyDescriptions;
    }
    @Override
    public String toString() {
        return "EvaluatedRuleDto [importedClassUris=" + importedClassUris + "\n"
                + "violatedRuleDescription=" + violatedRuleDescription + "\n"
                + "violationDescriptions=" + violationDescriptions + "\n"
                + "violatingClasses=" + violatingClasses + "\n"
                + "violationDependencyDescriptions=" + violationDependencyDescriptions + "]";
    }

}
