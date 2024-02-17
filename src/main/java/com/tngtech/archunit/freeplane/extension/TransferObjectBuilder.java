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

class TransferObjectBuilder {
    private final List<ViolationDescription> violationDescriptions;
    private final Map<String, Set<JavaClass>> violatingClasses;
    private SortedSet<String> violationDependencyDescriptions;
    private SortedSet<String> cyclicDependencyDescriptions;
    private SortedSet<String> violatingClassesGroup;

    TransferObjectBuilder() {
        this.violationDescriptions = new ArrayList<>();
        this.violatingClasses = new HashMap<>();
        this.violationDependencyDescriptions = Collections.emptySortedSet();
        this.cyclicDependencyDescriptions = Collections.emptySortedSet();
        this.violatingClassesGroup = Collections.emptySortedSet();
    }

    void handle(Collection<Object> violatingObjects, String message) {
        violationDependencyDescriptions = new TreeSet<>();
        cyclicDependencyDescriptions = new TreeSet<>();
        violatingClassesGroup = new TreeSet<>();
        violatingObjects.forEach(this::handle);
        violationDescriptions.add(new ViolationDescription(message, violationDependencyDescriptions, cyclicDependencyDescriptions, violatingClassesGroup));
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
            if(originOwner.equals(targetOwner))
                violatingClassesGroup.add(originOwner.getName());
            else
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
            final JavaClass violatingClass = (JavaClass)violatingObject;
            set.add(violatingClass);
            violatingClassesGroup.add(violatingClass.getName());
            return;
        }
        if(violatingObject instanceof JavaMember) {
            final Set<JavaClass> set = violatingClassSet("");
            final JavaClass violatingClass = ((JavaMember)violatingObject).getOwner();
            set.add(violatingClass);
            violatingClassesGroup.add(violatingClass.getName());
            return;
        }

        if(violatingObject instanceof Cycle<?>) {
            final Cycle<?> cycle = (Cycle<?>)violatingObject;
            cycle.getEdges().forEach(edge -> {
                final Object originObject = edge.getOrigin();
                final Object targetObject = edge.getTarget();
                if(originObject instanceof Slice && targetObject instanceof Slice) {
                    Slice origin = ((Slice)originObject);
                    Slice target = ((Slice)targetObject);
                    Set<JavaClass> originClasses = violatingClassSet(origin.getDescription());
                    Set<JavaClass> targetClasses = violatingClassSet(target.getDescription());
                    origin.getDependenciesFromSelf().stream()
                            .filter(d -> target.contains(d.getTargetClass()))
                            .forEach(d -> {
                                cyclicDependencyDescriptions.add(d.getDescription());
                                originClasses.add(d.getOriginClass());
                                targetClasses.add(d.getTargetClass());
                            });
                } else if (originObject instanceof ArchModule<?> && targetObject instanceof ArchModule<?>) {
                    ArchModule<?> origin = (ArchModule<?>)originObject;
                    ArchModule<?> target = (ArchModule<?>)targetObject;
                    Set<JavaClass> originClasses = violatingClassSet(origin.getName());
                    Set<JavaClass> targetClasses = violatingClassSet(target.getName());
                    origin.getModuleDependenciesFromSelf()
                    .stream()
                    .filter(d -> target.equals(d.getTarget()))
                    .flatMap(d -> d.toClassDependencies().stream())
                    .forEach(d -> {
                        cyclicDependencyDescriptions.add(d.getDescription());
                        originClasses.add(d.getOriginClass());
                        targetClasses.add(d.getTargetClass());
                    });
                }
            });
            return;
        }

        if(violatingObject instanceof SliceDependency) {
            final SliceDependency dependency = (SliceDependency)violatingObject;
            Slice origin = dependency.getOrigin();
            Slice target = dependency.getTarget();
            Set<JavaClass> originClasses = violatingClassSet(origin.getDescription());
            Set<JavaClass> targetClasses = violatingClassSet(target.getDescription());
            origin.stream()
                    .map(JavaClass::getDirectDependenciesFromSelf)
                    .flatMap(Set::stream)
                    .filter(d -> target.contains(d.getTargetClass()))
                    .forEach(d -> {
                        originClasses.add(d.getOriginClass());
                        targetClasses.add(d.getTargetClass());
                        violationDependencyDescriptions.add(d.getDescription());
                    });
            return;
        }

        if(violatingObject instanceof ModuleDependency<?>) {
            final ModuleDependency<?> dependency = (ModuleDependency<?>)violatingObject;
            ArchModule<?> origin = dependency.getOrigin();
            ArchModule<?> target = dependency.getTarget();
            Set<JavaClass> originClasses = violatingClassSet(origin.getName());
            Set<JavaClass> targetClasses = violatingClassSet(target.getName());
            dependency.toClassDependencies()
            .forEach(d -> {
                originClasses.add(d.getOriginClass());
                targetClasses.add(d.getTargetClass());
                violationDependencyDescriptions.add(d.getDescription());
            });
            return;
        }
        return;
    }

    private Set<JavaClass> violatingClassSet(String name) {
        return violatingClasses.computeIfAbsent(name, key -> new HashSet<>());
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
                violationDescriptions);
    }
}
