package com.tngtech.archunit.freeplane.extension;

import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.library.cycle_detection.Cycle;
import com.tngtech.archunit.library.dependencies.Slice;
import com.tngtech.archunit.library.dependencies.SliceDependency;
import com.tngtech.archunit.library.modules.ArchModule;
import com.tngtech.archunit.library.modules.ModuleDependency;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TransferObjectBuilder {
    private final List<String> violationDescriptions;
    private final Map<String, Set<JavaClass>> violatingClasses;
    private final Set<String> violationDependencyDescriptions;
    private boolean isNoCyclesConditionChecked;

    TransferObjectBuilder() {
        this.violationDescriptions = new ArrayList<>();
        this.violatingClasses = new HashMap<>();
        this.violationDependencyDescriptions = new TreeSet<>();
        this.isNoCyclesConditionChecked = false;
    }

    void handle(Collection<Object> violatingObjects, String message) {
        violationDescriptions.add(message);
        violatingObjects.forEach(this::handle);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void handle(Object violatingObject) {
        if(violatingObject instanceof JavaAccess<?>) {
            final JavaAccess<?> javaAccess = (JavaAccess<?>)violatingObject;
            final JavaClass originOwner = javaAccess.getOriginOwner();
            final JavaClass targetOwner = javaAccess.getTargetOwner();
            final Set<JavaClass> set = violatingClassSet("");
            set.add(originOwner);
            set.add(targetOwner);
            violationDependencyDescriptions.add(javaAccess.getDescription());
            return;
        }
        if(violatingObject instanceof Dependency) {
            final Dependency dependency = (Dependency)violatingObject;
            final JavaClass originOwner = dependency.getOriginClass();
            final JavaClass targetOwner = dependency.getTargetClass();
            final Set<JavaClass> set = violatingClassSet("");
            set.add(originOwner);
            set.add(targetOwner);
            violationDependencyDescriptions.add(dependency.getDescription());
            return;
        }
        if(violatingObject instanceof JavaClass) {
            final Set<JavaClass> set = violatingClassSet("");
            set.add((JavaClass)violatingObject);
            return;
        }
        if(violatingObject instanceof JavaMember) {
            final Set<JavaClass> set = violatingClassSet("");
            set.add(((JavaMember)violatingObject).getOwner());
            return;
        }

        if(violatingObject instanceof Cycle<?>) {
            isNoCyclesConditionChecked = true;
            final Cycle<?> cycle = (Cycle<?>)violatingObject;
            cycle.getEdges().forEach(edge -> {
                final Object origin = edge.getOrigin();
                final Object target = edge.getTarget();
                if(origin instanceof Slice && target instanceof Slice)
                    addViolatingClasses((Slice) origin, (Slice) target);
                else if (origin instanceof ArchModule<?> && target instanceof ArchModule<?>)
                    addViolatingClasses((ArchModule<?>) origin, (ArchModule<?>) target);
            });
            return;
        }
        if(violatingObject instanceof SliceDependency) {
            final SliceDependency dependency = (SliceDependency)violatingObject;
            addViolatingClasses(dependency.getOrigin(), dependency.getTarget());

            String description = dependency.getDescription();
            Stream.of(description.split(System.lineSeparator()))
                    .skip(1)
                    .forEach(violationDependencyDescriptions::add);
            return;
        }
        if(violatingObject instanceof ModuleDependency<?>) {
            final ModuleDependency<?> dependency = (ModuleDependency<?>)violatingObject;
            ArchModule<?> origin = dependency.getOrigin();
            ArchModule<?> target = dependency.getTarget();
            addViolatingClasses(origin, target);

            dependency.toClassDependencies().stream()
                    .map(Dependency::getDescription)
                    .forEach(violationDependencyDescriptions::add);
            return;
        }
        return;
    }

    private void addViolatingClasses(ArchModule<?> origin, ArchModule<?> target) {
        Stream<JavaClass> violatingOriginClasses = getViolatingOriginClasses(origin, target);
        Stream<JavaClass> violatingTargetClasses = getViolatingTargetClasses(origin, target);
        addViolatingClasses(origin.getName(), violatingOriginClasses, target.getName(), violatingTargetClasses);
    }

    private void addViolatingClasses(String originName,
                                            Stream<JavaClass> violatingOriginClasses,
                                            String targetName,
                                            Stream<JavaClass> violatingTargetClasses) {
        final Set<JavaClass> originSet = violatingClassSet(originName);
        violatingOriginClasses.forEach(originSet::add);
        final Set<JavaClass> targetSet = violatingClassSet( targetName);
        violatingTargetClasses.forEach(targetSet::add);
    }

    private void addViolatingClasses(Slice origin, Slice target) {
        Stream<JavaClass> violatingOriginClasses = getViolatingOriginClasses(origin, target);
        Stream<JavaClass> violatingTargetClasses = getViolatingTargetClasses(origin, target);
        addViolatingClasses(origin.getDescription(), violatingOriginClasses, target.getDescription(), violatingTargetClasses);
    }

    private Set<JavaClass> violatingClassSet(String name) {
        return violatingClasses.computeIfAbsent(name, key -> new HashSet<>());
    }

    private static Stream<JavaClass> getViolatingOriginClasses(Set<JavaClass> origin, Set<JavaClass> target) {
        return target.stream()
                .map(JavaClass::getDirectDependenciesToSelf)
                .flatMap(Collection::stream)
                .map(Dependency::getOriginClass)
                .filter(origin::contains);
    }

    private static Stream<JavaClass> getViolatingTargetClasses(Set<JavaClass> origin, Set<JavaClass> target) {
        return origin.stream()
                .map(JavaClass::getDirectDependenciesFromSelf)
                .flatMap(Collection::stream)
                .map(Dependency::getTargetClass)
                .filter(target::contains);
    }


    ArchitectureViolations buildTransferObject(String ruleDescription) {
        Map<String, Set<String>> locationSpecs = violatingClasses
                .entrySet().stream()
                .map(e -> new SimpleEntry<>(e.getKey(),
                        e.getValue().stream().map(JavaClass::getSource)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(Source::getUri)
                                .map(Object::toString)
                                .collect(Collectors.toSet())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new ArchitectureViolations(ruleDescription, locationSpecs,
                violationDescriptions, violationDependencyDescriptions, isNoCyclesConditionChecked);
    }
}
