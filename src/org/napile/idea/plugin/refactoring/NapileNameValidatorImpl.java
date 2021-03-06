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

package org.napile.idea.plugin.refactoring;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.descriptors.VariableDescriptor;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapileFile;
import org.napile.compiler.lang.psi.NapileVisitorVoid;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.idea.plugin.module.ModuleAnalyzerUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;

/**
 * User: Alefas
 * Date: 07.02.12
 */
public class NapileNameValidatorImpl implements NapileNameValidator
{
	public static NapileNameValidator getEmptyValidator(final Project project)
	{
		return new NapileNameValidator()
		{
			@Override
			public String validateName(String name)
			{
				return name;
			}

			@Override
			public Project getProject()
			{
				return project;
			}
		};
	}

	private final PsiElement myContainer;
	private PsiElement myAnchor;
	BindingTrace myBindingContext;

	public NapileNameValidatorImpl(PsiElement container, PsiElement anchor)
	{
		myContainer = container;
		myAnchor = anchor;
	}

	@Nullable
	@Override
	public String validateName(String name)
	{
		if(validateInner(name))
			return name;
		int i = 1;
		while(true)
		{
			if(validateInner(name + i))
				return name + i;
			++i;
		}
	}

	private boolean validateInner(String name)
	{
		PsiElement sibling;
		if(myAnchor != null)
		{
			sibling = myAnchor;
		}
		else
		{
			if(myContainer instanceof NapileExpression)
			{
				return checkElement(name, myContainer);
			}
			sibling = myContainer.getFirstChild();
		}

		while(sibling != null)
		{
			if(!checkElement(name, sibling))
				return false;
			sibling = sibling.getNextSibling();
		}

		return true;
	}

	private boolean checkElement(final String name, PsiElement sibling)
	{
		if(myBindingContext == null)
		{
			myBindingContext = ModuleAnalyzerUtil.lastAnalyze((NapileFile) myContainer.getContainingFile()).getBindingTrace();
		}
		final Ref<Boolean> result = new Ref<Boolean>(true);
		NapileVisitorVoid visitor = new NapileVisitorVoid()
		{
			@Override
			public void visitElement(PsiElement element)
			{
				if(result.get())
				{
					element.acceptChildren(this);
				}
			}

			@Override
			public void visitExpression(NapileExpression expression)
			{
				Collection<DeclarationDescriptor> variants = Collections.emptyList();
				for(DeclarationDescriptor variant : variants)
				{
					if(variant.getName().getName().equals(name) && variant instanceof VariableDescriptor)
					{
						result.set(false);
						return;
					}
				}
				super.visitExpression(expression);
			}
		};
		sibling.accept(visitor);
		return result.get();
	}

	@Override
	public Project getProject()
	{
		return myContainer.getProject();
	}
}
