package com.hideji.hayakawa.backendguidelines;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class InspectorJava extends AbstractBaseJavaLocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitForStatement(@NotNull PsiForStatement statement) {
                super.visitForStatement(statement);

                Optional<PsiElement> element = Arrays.stream(statement.getChildren())
                        .filter(c -> c instanceof PsiKeyword)
                        .findFirst();

                if (element.isPresent() && !(element.get().getNextSibling() instanceof PsiWhiteSpace)) {
                    holder.registerProblem(statement, "missing space after for");
                }
            }
        };
    }
}
