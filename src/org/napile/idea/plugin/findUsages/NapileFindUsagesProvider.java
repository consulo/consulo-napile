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

package org.napile.idea.plugin.findUsages;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapileCallParameterAsVariable;
import org.napile.compiler.lang.psi.NapileNamedDeclaration;
import org.napile.compiler.lang.psi.NapileNamedMethodOrMacro;
import org.napile.compiler.lang.psi.NapileClass;
import org.napile.compiler.lang.psi.NapileConstructor;
import org.napile.compiler.lang.psi.NapileVariable;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

/**
 * @author yole
 */
public class NapileFindUsagesProvider implements FindUsagesProvider
{
	@Override
	public boolean canFindUsagesFor(@NotNull PsiElement psiElement)
	{
		return psiElement instanceof NapileNamedDeclaration;
	}

	@Override
	public WordsScanner getWordsScanner()
	{
		return new NapileWordsScanner();
	}


	@Override
	public String getHelpId(@NotNull PsiElement psiElement)
	{
		return null;
	}

	@NotNull
	@Override
	public String getType(@NotNull PsiElement psiElement)
	{
		if(psiElement instanceof NapileNamedMethodOrMacro)
			return "method";
		if(psiElement instanceof NapileConstructor)
			return "constructor";
		if(psiElement instanceof NapileClass)
			return "class";
		if(psiElement instanceof NapileCallParameterAsVariable)
			return "parameter";
		if(psiElement instanceof NapileVariable)
			return "property";
		return "";
	}

	@NotNull
	@Override
	public String getDescriptiveName(@NotNull PsiElement psiElement)
	{
		if(psiElement instanceof PsiNamedElement)
		{
			final String name = ((PsiNamedElement) psiElement).getName();
			return name == null ? "<unnamed>" : name;
		}
		return "";
	}

	@NotNull
	@Override
	public String getNodeText(@NotNull PsiElement psiElement, boolean useFullName)
	{
		return getDescriptiveName(psiElement);
	}
}
