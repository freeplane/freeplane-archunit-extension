/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedSet;
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
        SortedSet<String> locationSpecs = evaluatedRule.getClasses().stream()
        .map(JavaClass::getSource)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Source::getUri)
        .map(Object::toString)
        .collect(Collectors.toCollection(TreeSet::new));
        List<String> violationDescriptions = new ArrayList<>();
        SortedSet<String> violatingClasses = new TreeSet<>();
        SortedSet<String> violationDependencyDescriptions = new TreeSet<>();
        result.handleViolations((violatingObjects, message) -> handle(violatingObjects, message, violationDescriptions, violatingClasses, violationDependencyDescriptions));
        final EvaluatedRuleDto data = new EvaluatedRuleDto(locationSpecs, evaluatedRule.getRule().getDescription(),
                violationDescriptions, violatingClasses, violationDependencyDescriptions);
        freeplaneClient.sendJson(data);
        System.out.println(data);
        System.out.println();

//        final List<Location> classLocations = locationSpecs.stream()
//        .map(x -> {
//            try {
//                return new URI(x);
//            } catch (URISyntaxException e) {
//                throw new IllegalArgumentException(e);
//            }
//        })
//        .map(Location::of)
//        .collect(Collectors.toList());
//        final JavaClasses importedClasses = new ClassFileImporter().importLocations(classLocations);
//        System.out.println(importedClasses.size());

    }

    private void handle(Collection<Object> violatingObjects, String message, Collection<String> violationDescriptions, Collection<String> violatingClasses, Collection<String> violationDependencyDescriptions) {
        violationDescriptions.add(message);
        violatingObjects.forEach(violatingObject -> handle(violatingObject, violatingClasses, violationDependencyDescriptions));
    }

    private void handle(Object violatingObject, Collection<String> violatingClasses, Collection<String> violationDependencyDescriptions) {
        if(violatingObject instanceof JavaAccess<?>) {
            final JavaAccess<?> javaAccess = (JavaAccess<?>)violatingObject;
            final JavaClass originOwner = javaAccess.getOriginOwner();
            final JavaClass targetOwner = javaAccess.getTargetOwner();
            violatingClasses.add(originOwner.getFullName());
            violatingClasses.add(targetOwner.getFullName());
            violationDependencyDescriptions.add(javaAccess.getDescription());
            return;
        }
        if(violatingObject instanceof Cycle<?>) {
            final Cycle<?> cycle = (Cycle<?>)violatingObject;
            cycle.getEdges().forEach(edge -> {
                final Object origin = edge.getOrigin();
                if(origin instanceof Slice)
                    ((Slice)origin).forEach(c -> violatingClasses.add(c.getFullName()));
                else if (origin instanceof ArchModule<?>) {
                 ((ArchModule<?>)origin).forEach(c -> violatingClasses.add(c.getFullName()));
                }
            });
            return;
        }
    }

}