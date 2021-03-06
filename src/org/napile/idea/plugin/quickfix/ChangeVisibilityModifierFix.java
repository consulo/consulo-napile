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

package org.napile.idea.plugin.quickfix;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.descriptors.CallableMemberDescriptor;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.descriptors.Visibilities;
import org.napile.compiler.lang.descriptors.Visibility;
import org.napile.compiler.lang.diagnostics.Diagnostic;
import org.napile.compiler.lang.resolve.BindingTraceKeys;
import org.napile.compiler.lang.lexer.NapileKeywordToken;
import org.napile.compiler.lang.lexer.NapileToken;
import org.napile.compiler.lang.lexer.NapileTokens;
import org.napile.compiler.lang.psi.NapileFile;
import org.napile.compiler.lang.psi.NapileModifierListOwner;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.idea.plugin.NapileBundle;
import org.napile.idea.plugin.module.ModuleAnalyzerUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author svtk
 */
public class ChangeVisibilityModifierFix extends NapileIntentionAction<NapileModifierListOwner>
{
	public ChangeVisibilityModifierFix(@NotNull NapileModifierListOwner element)
	{
		super(element);
	}

	@NotNull
	@Override
	public String getText()
	{
		return NapileBundle.message("change.visibility.modifier");
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return NapileBundle.message("change.visibility.modifier");
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
	{
		if(!(file instanceof NapileFile))
			return false;
		return super.isAvailable(project, editor, file) && (findVisibilityChangeTo((NapileFile) file) != null);
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		if(!(file instanceof NapileFile))
			return;
		NapileKeywordToken modifier = findVisibilityChangeTo((NapileFile) file);
		NapileToken[] modifiersThanCanBeReplaced = new NapileKeywordToken[]{
				NapileTokens.LOCAL_KEYWORD,
				NapileTokens.COVERED_KEYWORD
		};
		element.replace(AddModifierFix.addModifier(element, modifier, modifiersThanCanBeReplaced, project, true));
	}

	private NapileKeywordToken findVisibilityChangeTo(NapileFile file)
	{
		BindingTrace bindingContext = ModuleAnalyzerUtil.lastAnalyze(file).getBindingTrace();
		DeclarationDescriptor descriptor;
		descriptor = bindingContext.get(BindingTraceKeys.DECLARATION_TO_DESCRIPTOR, element);

		if(!(descriptor instanceof CallableMemberDescriptor))
			return null;

		CallableMemberDescriptor memberDescriptor = (CallableMemberDescriptor) descriptor;
		Visibility maxVisibility = null;
		for(CallableMemberDescriptor overriddenDescriptor : memberDescriptor.getOverriddenDescriptors())
		{
			Visibility overriddenDescriptorVisibility = overriddenDescriptor.getVisibility();
			if(maxVisibility == null)
			{
				maxVisibility = overriddenDescriptorVisibility;
				continue;
			}
			Integer compare = Visibilities.compare(maxVisibility, overriddenDescriptorVisibility);
			if(compare == null)
			{
				maxVisibility = Visibility.PUBLIC;
			}
			else if(compare < 0)
			{
				maxVisibility = overriddenDescriptorVisibility;
			}
		}
		if(maxVisibility == memberDescriptor.getVisibility())
		{
			return null;
		}
		NapileKeywordToken modifier = null;
		if(maxVisibility == Visibility.COVERED)
		{
			modifier = NapileTokens.COVERED_KEYWORD;
		}

		return modifier;
	}

	public static NapileIntentionActionFactory createFactory()
	{
		return new NapileIntentionActionFactory()
		{
			@Override
			public NapileIntentionAction<NapileModifierListOwner> createAction(Diagnostic diagnostic)
			{
				PsiElement element = diagnostic.getPsiElement();
				if(!(element instanceof NapileModifierListOwner))
					return null;
				return new ChangeVisibilityModifierFix((NapileModifierListOwner) element);
			}
		};
	}
}
