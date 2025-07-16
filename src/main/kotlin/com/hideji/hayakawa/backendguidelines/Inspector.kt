package com.hideji.hayakawa.backendguidelines

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiForStatement
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.PsiKeyword
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiSwitchStatement
import com.intellij.psi.PsiVariable
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.idea.base.util.reformat

class Inspector : AbstractBaseJavaLocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitForStatement(statement: PsiForStatement) {
                super.visitForStatement(statement)
                handleStatement(statement, holder)
            }

            override fun visitIfStatement(statement: PsiIfStatement) {
                super.visitIfStatement(statement)
                handleStatement(statement, holder)
            }

            override fun visitSwitchStatement(statement: PsiSwitchStatement) {
                super.visitSwitchStatement(statement)
                handleStatement(statement, holder)
            }

            override fun visitVariable(variable: PsiVariable) {
                super.visitVariable(variable)
                if (variable.modifierList!!.children.none() { c -> c.text.equals("final") })
                    holder.registerProblem(variable, "consider setting variable as final", SetAsFinal())
            }

            fun handleStatement(statement: PsiStatement, holder: ProblemsHolder) {
                if (statement.children.find { c -> c is PsiKeyword }?.nextSibling !is PsiWhiteSpace) {
                    holder.registerProblem(statement, "missing space before (", Reformat())
                }
                if (statement.lastChild.prevSibling !is PsiWhiteSpace) {
                    holder.registerProblem(statement, "missing space before {", Reformat())
                }
                if (statement.lastChild.firstChild !is PsiCodeBlock) {
                    holder.registerProblem(statement, "missing {")
                }
            }

            override fun visitExpression(expression: PsiExpression) {
                super.visitExpression(expression)

                for (child in expression.children) {
                    if (child is PsiLiteralExpression)
                        holder.registerProblem(child, "avoid hardcoded")
                }
            }

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                for (expression in expression.argumentList.expressions){
                    if (expression is PsiLiteralExpression)
                        holder.registerProblem(expression, "avoid hardcoded")
                }
            }
        }
    }

    inner class Reformat: LocalQuickFix {
        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor
        ) {
            val element = descriptor.psiElement
            element.reformat(true)
        }
        override fun getName() = "Reformat"
        override fun getFamilyName() = "Hello"
    }

    inner class SetAsFinal: LocalQuickFix {
        override fun applyFix(
            project: Project,
            descriptor: ProblemDescriptor
        ) {
            val element = descriptor.psiElement
            (element as PsiVariable).modifierList!!.setModifierProperty(PsiModifier.FINAL, true)
        }
        override fun getName() = "Set as Final"
        override fun getFamilyName() = "Hello"
    }
}