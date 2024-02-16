package com.tngtech.archunit.freeplane.extension;

import java.util.Collections;
import java.util.SortedSet;

public class ViolationDescription {
    private String fullDescription;
    private SortedSet<String> violationDependencyDescriptions;
    private SortedSet<String> cyclicDependencyDescriptions;

    public ViolationDescription(String fullDescription, SortedSet<String> violationDependencyDescriptions,
                                SortedSet<String> cyclicDependencyDescriptions) {
        this.fullDescription = fullDescription;
        this.violationDependencyDescriptions = violationDependencyDescriptions;
        this.cyclicDependencyDescriptions = cyclicDependencyDescriptions;
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



    @Override
    public String toString() {
        return "ViolationDescription{" +
                "fullDescription='" + fullDescription + '\'' +
                ", violationDependencyDescriptions=" + getViolationDependencyDescriptions() +
                ", cyclicDependencyDescriptions=" + getCyclicDependencyDescriptions() +
                '}';
    }
}
