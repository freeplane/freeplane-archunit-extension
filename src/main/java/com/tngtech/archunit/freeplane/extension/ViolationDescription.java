package com.tngtech.archunit.freeplane.extension;

import java.util.SortedSet;

public class ViolationDescription {
    final public String fullDescription;
    final public SortedSet<String> violationDependencyDescriptions;

    public ViolationDescription(String fullDescription, SortedSet<String> violationDependencyDescriptions) {
        this.fullDescription = fullDescription;
        this.violationDependencyDescriptions = violationDependencyDescriptions;
    }

    @Override
    public String toString() {
        return "ViolationDescription{" +
                "fullDescription='" + fullDescription + '\'' +
                ", violationDependencyDescriptions=" + violationDependencyDescriptions +
                '}';
    }
}
