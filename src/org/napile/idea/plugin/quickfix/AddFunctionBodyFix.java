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
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.diagnostics.Diagnostic;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapileMethod;
import org.napile.compiler.lang.psi.NapilePsiFactory;
import org.napile.idea.plugin.NapileBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author svtk
 */
public class AddFunctionBodyFix extends NapileIntentionAction<NapileMethod>
{
	public AddFunctionBodyFix(@NotNull NapileMethod element)
	{
		super(element);
	}

	@NotNull
	@Override
	public String getText()
	{
		return NapileBundle.message("add.function.body");
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return NapileBundle.message("add.function.body");
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
	{
		return super.isAvailable(project, editor, file) && element.getBodyExpression() == null;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		NapileMethod newElement = (NapileMethod) element.copy();
		NapileExpression bodyExpression = newElement.getBodyExpression();
		if(!(newElement.getLastChild() instanceof PsiWhiteSpace))
		{
			newElement.add(NapilePsiFactory.createWhiteSpace(project));
		}
		if(bodyExpression == null)
		{
			newElement.add(NapilePsiFactory.createEmptyBody(project));
		}
		element.replace(newElement);
	}

	public static NapileIntentionActionFactory createFactory()
	{
		return new NapileIntentionActionFactory()
		{
			@Nullable
			@Override
			public NapileIntentionAction createAction(Diagnostic diagnostic)
			{
				PsiElement element = diagnostic.getPsiElement();
				NapileMethod function = PsiTreeUtil.getParentOfType(element, NapileMethod.class, false);
				if(function == null)
					return null;
				return new AddFunctionBodyFix(function);
			}
		};
	}
}
