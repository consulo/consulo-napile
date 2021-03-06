/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.napile.idea.plugin.intentions;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.descriptors.SimpleMethodDescriptor;
import org.napile.compiler.lang.descriptors.VariableDescriptor;
import org.napile.compiler.lang.diagnostics.Diagnostic;
import org.napile.compiler.lang.diagnostics.Errors;
import org.napile.compiler.lang.lexer.NapileNodes;
import org.napile.compiler.lang.psi.*;
import org.napile.compiler.lang.resolve.BindingTraceKeys;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.compiler.lang.types.ErrorUtils;
import org.napile.compiler.lang.types.NapileType;
import org.napile.compiler.render.DescriptorRenderer;
import org.napile.idea.plugin.NapileBundle;
import org.napile.idea.plugin.codeInsight.ReferenceToClassesShortening;
import org.napile.idea.plugin.module.ModuleAnalyzerUtil;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author Evgeny Gerashchenko
 * @since 4/20/12
 */
public class SpecifyTypeExplicitlyAction extends PsiElementBaseIntentionAction
{
	@NotNull
	@Override
	public String getFamilyName()
	{
		return NapileBundle.message("specify.type.explicitly.action.family.name");
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		NapileTypeReference typeRefParent = PsiTreeUtil.getTopmostParentOfType(element, NapileTypeReference.class);
		if(typeRefParent != null)
		{
			element = typeRefParent;
		}
		PsiElement parent = element.getParent();
		NapileType type = getTypeForDeclaration((NapileNamedDeclaration) parent);
		if(ErrorUtils.isErrorType(type))
		{
			return;
		}
		if(parent instanceof NapileVariable)
		{
			NapileVariable property = (NapileVariable) parent;
			if(property.getType() == null)
			{
				addTypeAnnotation(project, property, type);
			}
			else
			{
				removeTypeAnnotation(property);
			}
		}
		else if(parent instanceof NapileCallParameterAsVariable)
		{
			NapileCallParameterAsVariable parameter = (NapileCallParameterAsVariable) parent;
			if(parameter.getTypeReference() == null)
			{
				addTypeAnnotation(project, parameter, type);
			}
			else
			{
				removeTypeAnnotation(parameter);
			}
		}
		else if(parent instanceof NapileNamedMethodOrMacro)
		{
			NapileNamedMethodOrMacro function = (NapileNamedMethodOrMacro) parent;
			assert function.getReturnTypeRef() == null;
			addTypeAnnotation(project, function, type);
		}
		else
		{
			assert false;
		}
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		NapileTypeReference typeRefParent = PsiTreeUtil.getTopmostParentOfType(element, NapileTypeReference.class);
		if(typeRefParent != null)
		{
			element = typeRefParent;
		}
		PsiElement parent = element.getParent();
		if(!(parent instanceof NapileNamedDeclaration))
		{
			return false;
		}
		NapileNamedDeclaration declaration = (NapileNamedDeclaration) parent;
		if(declaration instanceof NapileVariable && !PsiTreeUtil.isAncestor(((NapileVariable) declaration).getInitializer(), element, false))
		{
			if(((NapileVariable) declaration).getType() != null)
			{
				setText(NapileBundle.message("specify.type.explicitly.remove.action.name"));
				return true;
			}
			else
			{
				setText(NapileBundle.message("specify.type.explicitly.add.action.name"));
			}
		}
		else if(declaration instanceof NapileNamedMethodOrMacro && ((NapileNamedMethodOrMacro) declaration).getReturnTypeRef() == null && !((NapileNamedMethodOrMacro) declaration).hasBlockBody())
		{
			setText(NapileBundle.message("specify.type.explicitly.add.return.type.action.name"));
		}
		else if(declaration instanceof NapileCallParameterAsVariable && NapileNodes.CALL_PARAMETER_AS_VARIABLE == declaration.getNode().getElementType())
		{
			if(((NapileCallParameterAsVariable) declaration).getTypeReference() != null)
			{
				setText(NapileBundle.message("specify.type.explicitly.remove.action.name"));
				return true;
			}
			else
			{
				setText(NapileBundle.message("specify.type.explicitly.add.action.name"));
			}
		}
		else
		{
			return false;
		}

		if(ErrorUtils.isErrorType(getTypeForDeclaration(declaration)))
		{
			return false;
		}
		return !isDisabledForError() || !hasPublicMemberDiagnostic(declaration);
	}


	private static boolean hasPublicMemberDiagnostic(@NotNull NapileNamedDeclaration declaration)
	{
		BindingTrace bindingContext = ModuleAnalyzerUtil.lastAnalyze((NapileFile) declaration.getContainingFile()).getBindingTrace();
		for(Diagnostic diagnostic : bindingContext.getDiagnostics())
		{
			//noinspection ConstantConditions
			if(Errors.PUBLIC_MEMBER_SHOULD_SPECIFY_TYPE == diagnostic.getFactory() && declaration == diagnostic.getPsiElement())
			{
				return true;
			}
		}
		return false;
	}

	@NotNull
	private static NapileType getTypeForDeclaration(@NotNull NapileNamedDeclaration declaration)
	{
		BindingTrace bindingContext = ModuleAnalyzerUtil.lastAnalyze((NapileFile) declaration.getContainingFile()).getBindingTrace();
		DeclarationDescriptor descriptor = bindingContext.get(BindingTraceKeys.DECLARATION_TO_DESCRIPTOR, declaration);

		NapileType type;
		if(descriptor instanceof VariableDescriptor)
		{
			type = ((VariableDescriptor) descriptor).getType();
		}
		else if(descriptor instanceof SimpleMethodDescriptor)
		{
			type = ((SimpleMethodDescriptor) descriptor).getReturnType();
		}
		else
		{
			return ErrorUtils.createErrorType("unknown declaration type");
		}

		return type == null ? ErrorUtils.createErrorType("null type") : type;
	}

	protected boolean isDisabledForError()
	{
		return true;
	}

	public static void addTypeAnnotation(Project project, NapileVariable property, @NotNull NapileType exprType)
	{
		if(property.getType() != null)
			return;
		PsiElement anchor = property.getNameIdentifier();
		if(anchor == null)
			return;
		anchor = anchor.getNextSibling();
		if(anchor != null)
		{
			if(!(anchor instanceof PsiWhiteSpace))
			{
				return;
			}
		}
		NapileTypeReference typeReference = NapilePsiFactory.createType(project, DescriptorRenderer.TEXT.renderType(exprType));
		ASTNode colon = NapilePsiFactory.createColonNode(project);
		ASTNode anchorNode = anchor == null ? null : anchor.getNode().getTreeNext();
		if(anchor == null)
		{
			property.getNode().addChild(NapilePsiFactory.createWhiteSpace(project).getNode(), anchorNode);
		}
		property.getNode().addChild(colon, anchorNode);
		property.getNode().addChild(NapilePsiFactory.createWhiteSpace(project).getNode(), anchorNode);
		property.getNode().addChild(typeReference.getNode(), anchorNode);
		property.getNode().addChild(NapilePsiFactory.createWhiteSpace(project).getNode(), anchorNode);
		if(anchor != null)
		{
			anchor.delete();
		}
		ReferenceToClassesShortening.compactReferenceToClasses(Collections.singletonList(property));
	}

	public static void addTypeAnnotation(Project project, NapileMethod function, @NotNull NapileType exprType)
	{
		NapileTypeReference typeReference = NapilePsiFactory.createType(project, DescriptorRenderer.TEXT.renderType(exprType));
		Pair<PsiElement, PsiElement> colon = NapilePsiFactory.createColon(project);
		NapileCallParameterList valueParameterList = function.getCallParameterList();
		assert valueParameterList != null;
		function.addAfter(typeReference, valueParameterList);
		function.addRangeAfter(colon.getFirst(), colon.getSecond(), valueParameterList);
		ReferenceToClassesShortening.compactReferenceToClasses(Collections.singletonList(function));
	}

	public static void addTypeAnnotation(Project project, NapileCallParameterAsVariable parameter, @NotNull NapileType exprType)
	{
		NapileTypeReference typeReference = NapilePsiFactory.createType(project, DescriptorRenderer.TEXT.renderType(exprType));
		Pair<PsiElement, PsiElement> colon = NapilePsiFactory.createColon(project);
		parameter.addAfter(typeReference, parameter.getNameIdentifier());
		parameter.addRangeAfter(colon.getFirst(), colon.getSecond(), parameter.getNameIdentifier());
		ReferenceToClassesShortening.compactReferenceToClasses(Collections.singletonList(parameter));
	}

	private static void removeTypeAnnotation(@NotNull NapileNamedDeclaration property, @Nullable NapileTypeReference typeReference)
	{
		if(typeReference == null)
			return;
		PsiElement identifier = property.getNameIdentifier();
		if(identifier == null)
			return;
		PsiElement sibling = identifier.getNextSibling();
		if(sibling == null)
			return;
		PsiElement nextSibling = typeReference.getNextSibling();
		sibling.getParent().getNode().removeRange(sibling.getNode(), nextSibling == null ? null : nextSibling.getNode());
	}

	public static void removeTypeAnnotation(NapileVariable property)
	{
		removeTypeAnnotation(property, property.getType());
	}

	public static void removeTypeAnnotation(NapileCallParameterAsVariable parameter)
	{
		removeTypeAnnotation(parameter, parameter.getTypeReference());
	}
}
