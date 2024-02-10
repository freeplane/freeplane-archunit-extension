/*
 * Created on 7 Feb 2024
 *
 * author dimitry
 */
package com.tngtech.archunit.freeplane.extension;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.modules.syntax.ModuleRuleDefinition.modules;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.freeplane.extension.a.A;
import com.tngtech.archunit.freeplane.extension.b.B;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.extension.EvaluatedRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SliceRule;

@ExtendWith(MockitoExtension.class)
public class FreeplaneExtensionTest {
    static private JavaClasses importedClasses = new ClassFileImporter()
            .importPackagesOf(A.class, B.class);

    private static EvaluatedRule evaluate(ArchRule rule) {
        return new EvaluatedRule() {

            private final EvaluationResult result = rule.evaluate(importedClasses);

            @Override
            public ArchRule getRule() {
                return rule;
            }

            @Override
            public EvaluationResult getResult() {
                return result;

            }

            @Override
            public JavaClasses getClasses() {
                return importedClasses;
            }
        };
    }

    @Mock FreeplaneClient freeplaneClient;
    @InjectMocks
    FreeplaneExtension uut;

    @BeforeEach
    void activateClient() {
        when(freeplaneClient.sendJson(any())).thenReturn(true);
    }

    @Test
    void handlesJavaAccessViolation() throws Exception {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().haveFullyQualifiedName(B.class.getName())
                .should().accessClassesThat().haveFullyQualifiedName(A.class.getName());

        uut.handle(evaluate(rule));

    }


    @Test
    void handlesSliceCycles() throws Exception {
        SliceRule rule = slices().matching("..(*)").namingSlices("$1").should().beFreeOfCycles();
        uut.handle(evaluate(rule));
    }

    @Test
    void handlesModuleCycles() throws Exception {
        ArchRule rule =
                modules()
                        .definedByAnnotation(AppModule.class, AppModule::value)
                        .should().beFreeOfCycles();
        uut.handle(evaluate(rule));
    }
}
