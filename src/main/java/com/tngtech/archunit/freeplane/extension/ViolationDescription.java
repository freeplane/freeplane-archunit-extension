package com.tngtech.archunit.freeplane.extension;

import java.util.Collections;
import java.util.SortedSet;

public class ViolationDescription {
    private String fullDescription;
    private SortedSet<String> violationDependencyDescriptions;
    private SortedSet<String> cyclicDependencyDescriptions;
    private SortedSet<String> violatingClasses;

    public ViolationDescription(String fullDescription, SortedSet<String> violationDependencyDescriptions,
                                SortedSet<String> cyclicDependencyDescriptions,
                                SortedSet<String> violatingClasses) {
        this.fullDescription = fullDescription;
        this.violationDependencyDescriptions = violationDependencyDescriptions;
        this.cyclicDependencyDescriptions = cyclicDependencyDescriptions;
        this.violatingClasses = violatingClasses;
    }



    public String getFullDescription() {
        return fullDescription;
    }

    public SortedSet<String> getViolationDependencyDescriptions() {
        if(violationDependencyDescriptions == null)
            violationDependencyDescriptions = Collections.emptySortedSet();
        return violationDependencyDescriptions;
    }


    public SortedSet<String> getCyclicDependencyDescriptions() {
        if(cyclicDependencyDescriptions == null)
            cyclicDependencyDescriptions = Collections.emptySortedSet();
        return cyclicDependencyDescriptions;
    }



    public SortedSet<String> getViolatingClasses() {
        if(violatingClasses == null)
            violatingClasses = Collections.emptySortedSet();
        return violatingClasses;
    }

}
