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
import org.napile.compiler.lang.diagnostics.Diagnostic;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapilePsiFactory;
import org.napile.compiler.lang.psi.NapileQualifiedExpressionImpl;
import org.napile.compiler.lang.psi.NapileSimpleNameExpression;
import org.napile.compiler.lang.psi.NapileThisExpression;
import org.napile.idea.plugin.NapileBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author svtk
 */
public class ChangeToBackingFieldFix extends NapileIntentionAction<NapileSimpleNameExpression>
{
	public ChangeToBackingFieldFix(@NotNull NapileSimpleNameExpression element)
	{
		super(element);
	}

	@NotNull
	@Override
	public String getText()
	{
		return NapileBundle.message("change.to.backing.field");
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return NapileBundle.message("change.to.backing.field");
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		NapileSimpleNameExpression backingField = (NapileSimpleNameExpression) NapilePsiFactory.createExpression(project, "$" + element.getText());
		element.replace(backingField);
	}

	public static NapileIntentionActionFactory createFactory()
	{
		return new NapileIntentionActionFactory()
		{
			@Override
			public NapileIntentionAction<NapileSimpleNameExpression> createAction(Diagnostic diagnostic)
			{
				NapileSimpleNameExpression expression = QuickFixUtil.getParentElementOfType(diagnostic, NapileSimpleNameExpression.class);
				if(expression == null)
				{
					PsiElement element = diagnostic.getPsiElement();
					if(element instanceof NapileQualifiedExpressionImpl && ((NapileQualifiedExpressionImpl) element).getReceiverExpression() instanceof NapileThisExpression)
					{
						NapileExpression selector = ((NapileQualifiedExpressionImpl) element).getSelectorExpression();
						if(selector instanceof NapileSimpleNameExpression)
						{
							expression = (NapileSimpleNameExpression) selector;
						}
					}
				}
				if(expression == null)
					return null;
				return new ChangeToBackingFieldFix(expression);
			}
		};
	}
}
