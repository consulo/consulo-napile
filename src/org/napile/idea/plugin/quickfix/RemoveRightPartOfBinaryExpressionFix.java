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
import org.napile.compiler.lang.psi.NapileBinaryExpression;
import org.napile.compiler.lang.psi.NapileBinaryExpressionWithTypeRHS;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.idea.plugin.NapileBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author svtk
 */
public class RemoveRightPartOfBinaryExpressionFix<T extends NapileExpression> extends NapileIntentionAction<T>
{
	private final String message;

	public RemoveRightPartOfBinaryExpressionFix(@NotNull T element, String message)
	{
		super(element);
		this.message = message;
	}

	public String getText()
	{
		return message;
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return NapileBundle.message("remove.right.part.of.binary.expression");
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		if(element instanceof NapileBinaryExpression)
		{
			NapileBinaryExpression newElement = (NapileBinaryExpression) element.copy();
			element.replace(newElement.getLeft());
		}
		else if(element instanceof NapileBinaryExpressionWithTypeRHS)
		{
			NapileBinaryExpressionWithTypeRHS newElement = (NapileBinaryExpressionWithTypeRHS) element.copy();
			element.replace(newElement.getLeft());
		}
	}

	public static NapileIntentionActionFactory createRemoveCastFactory()
	{
		return new NapileIntentionActionFactory()
		{
			@Override
			public NapileIntentionAction<NapileBinaryExpressionWithTypeRHS> createAction(Diagnostic diagnostic)
			{
				NapileBinaryExpressionWithTypeRHS expression = QuickFixUtil.getParentElementOfType(diagnostic, NapileBinaryExpressionWithTypeRHS.class);
				if(expression == null)
					return null;
				return new RemoveRightPartOfBinaryExpressionFix<NapileBinaryExpressionWithTypeRHS>(expression, NapileBundle.message("remove.cast"));
			}
		};
	}

	public static NapileIntentionActionFactory createRemoveElvisOperatorFactory()
	{
		return new NapileIntentionActionFactory()
		{
			@Override
			public NapileIntentionAction<NapileBinaryExpression> createAction(Diagnostic diagnostic)
			{
				NapileBinaryExpression expression = QuickFixUtil.getParentElementOfType(diagnostic, NapileBinaryExpression.class);
				if(expression == null)
					return null;
				return new RemoveRightPartOfBinaryExpressionFix<NapileBinaryExpression>(expression, NapileBundle.message("remove.elvis.operator"));
			}
		};
	}
}

