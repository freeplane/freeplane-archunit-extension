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

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.extension.ArchUnitExtension;
import com.tngtech.archunit.lang.extension.EvaluatedRule;
import com.tngtech.archunit.library.cycle_detection.Cycle;
import com.tngtech.archunit.library.dependencies.Slice;
import com.tngtech.archunit.library.modules.ArchModule;

public class Extension  implements ArchUnitExtension {
    public static final String UNIQUE_IDENTIFIER = "freeplane-archunit-extension";
    private FreeplaneClient freeplaneClient;

    public Extension(FreeplaneClient freeplaneClient) {
        this.freeplaneClient = freeplaneClient;
    }

    public Extension() {
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
        freeplaneClient.sendJson(data);
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
        if(violatingObject instanceof Cycle<?>) {
            final Cycle<?> cycle = (Cycle<?>)violatingObject;
            cycle.getEdges().forEach(edge -> {
                final Object origin = edge.getOrigin();
                if(origin instanceof Slice) {
                    final Set<JavaClass> set = violatingClasses.computeIfAbsent(((Slice)origin).getDescription(), key -> new HashSet<>());
                    ((Slice)origin).forEach(set::add);
                } else if (origin instanceof ArchModule<?>) {
                    final Set<JavaClass> set = violatingClasses.computeIfAbsent(((ArchModule<?>)origin).getName(), key -> new HashSet<>());
                    ((ArchModule<?>)origin).forEach(set::add);
                }
            });
            return;
        }
    }

}