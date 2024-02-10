/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.extension.ArchUnitExtension;
import com.tngtech.archunit.lang.extension.EvaluatedRule;
import com.tngtech.archunit.library.cycle_detection.Cycle;
import com.tngtech.archunit.library.dependencies.Slice;
import com.tngtech.archunit.library.dependencies.SliceDependency;
import com.tngtech.archunit.library.modules.ArchModule;
import com.tngtech.archunit.library.modules.ModuleDependency;

public class FreeplaneExtension implements ArchUnitExtension {
    public static final String UNIQUE_IDENTIFIER = "freeplane-archunit-extension";
    private FreeplaneClient freeplaneClient;

    public FreeplaneExtension(FreeplaneClient freeplaneClient) {
        this.freeplaneClient = freeplaneClient;
    }

    public FreeplaneExtension() {
        this(null);
    }

    @Override
    public String getUniqueIdentifier() {
        return UNIQUE_IDENTIFIER;
    }

    @Override
    public void configure(Properties properties) {
        final String portSpec = properties.getProperty("port", "6297");
        int port = Integer.parseInt(portSpec);
        freeplaneClient = new FreeplaneClient("localhost", port);
    }

    @Override
    public void handle(EvaluatedRule evaluatedRule) {
        final EvaluationResult result = evaluatedRule.getResult();
        if(! result.hasViolation())
            return;
        List<String> violationDescriptions = new ArrayList<>();
        Map<String, Set<JavaClass>> violatingClasses = new HashMap<>();
        Set<String> violationDependencyDescriptions = new TreeSet<>();
        result.handleViolations((violatingObjects, message) -> handle(violatingObjects, message, violationDescriptions, violatingClasses, violationDependencyDescriptions));
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
       final ArchTestResult data = new ArchTestResult(evaluatedRule.getRule().getDescription(), locationSpecs,
                violationDescriptions, violationDependencyDescriptions);
        if(! freeplaneClient.sendJson(data))
            ArchConfiguration.get().configureExtension(FreeplaneExtension.UNIQUE_IDENTIFIER)
                .setProperty("enabled", false);
        System.out.println(data);
        System.out.println();
    }

    private void handle(Collection<Object> violatingObjects, String message, Collection<String> violationDescriptions, Map<String, Set<JavaClass>> violatingClasses, Collection<String> violationDependencyDescriptions) {
        violationDescriptions.add(message);
        violatingObjects.forEach(violatingObject -> handle(violatingObject, violatingClasses, violationDependencyDescriptions));
    }

    private void handle(Object violatingObject, Map<String, Set<JavaClass>> violatingClasses, Collection<String> violationDependencyDescriptions) {
        if(violatingObject instanceof JavaAccess<?>) {
            final JavaAccess<?> javaAccess = (JavaAccess<?>)violatingObject;
            final JavaClass originOwner = javaAccess.getOriginOwner();
            final JavaClass targetOwner = javaAccess.getTargetOwner();
            final Set<JavaClass> set = violatingClasses.computeIfAbsent("", key -> new HashSet<>());
            set.add(originOwner);
            set.add(targetOwner);
            violationDependencyDescriptions.add(javaAccess.getDescription());
            return;
        }
        if(violatingObject instanceof Dependency) {
            final Dependency dependency = (Dependency)violatingObject;
            final JavaClass originOwner = dependency.getOriginClass();
            final JavaClass targetOwner = dependency.getTargetClass();
            final Set<JavaClass> set = violatingClasses.computeIfAbsent("", key -> new HashSet<>());
            set.add(originOwner);
            set.add(targetOwner);
            violationDependencyDescriptions.add(dependency.getDescription());
            return;
        }
        if(violatingObject instanceof JavaClass) {
            final Set<JavaClass> set = violatingClasses.computeIfAbsent("", key -> new HashSet<>());
            set.add((JavaClass)violatingObject);
            return;
        }
        if(violatingObject instanceof JavaMember) {
            final Set<JavaClass> set = violatingClasses.computeIfAbsent("", key -> new HashSet<>());
            set.add(((JavaMember)violatingObject).getOwner());
            return;
        }

        if(violatingObject instanceof Cycle<?>) {
            final Cycle<?> cycle = (Cycle<?>)violatingObject;
            cycle.getEdges().forEach(edge -> {
                final Object origin = edge.getOrigin();
                if(origin instanceof Slice) {
                    final Set<JavaClass> set = violatingClasses.computeIfAbsent(((HasDescription)origin).getDescription(), key -> new HashSet<>());
                    set.addAll(((Slice) origin));
                } else if (origin instanceof ArchModule<?>) {
                    final Set<JavaClass> set = violatingClasses.computeIfAbsent(((ArchModule<?>)origin).getName(), key -> new HashSet<>());
                    set.addAll((ArchModule<?>) origin);
                }
            });
            return;
        }
        if(violatingObject instanceof SliceDependency) {
            final SliceDependency dependency = (SliceDependency)violatingObject;
            final Set<JavaClass> set = violatingClasses.computeIfAbsent("", key -> new HashSet<>());
            set.addAll(dependency.getOrigin());
            set.addAll(dependency.getTarget());
            violationDependencyDescriptions.add(dependency.getDescription());
            return;
        }
        if(violatingObject instanceof ModuleDependency<?>) {
            final ModuleDependency<?> dependency = (ModuleDependency<?>)violatingObject;
            final Set<JavaClass> set = violatingClasses.computeIfAbsent("", key -> new HashSet<>());
            set.addAll(dependency.getOrigin());
            set.addAll(dependency.getTarget());
            dependency.toClassDependencies().stream()
                    .map(Dependency::getDescription)
                    .forEach(violationDependencyDescriptions::add);
            return;
        }

        return;
    }

}