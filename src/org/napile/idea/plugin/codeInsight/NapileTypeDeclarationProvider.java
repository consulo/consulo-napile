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

package org.napile.idea.plugin.codeInsight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.napile.compiler.lang.descriptors.CallableDescriptor;
import org.napile.compiler.lang.descriptors.ClassifierDescriptor;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.resolve.BindingTraceKeys;
import org.napile.compiler.lang.resolve.BindingTraceUtil;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.compiler.lang.types.NapileType;
import org.napile.compiler.lang.psi.NapileElement;
import org.napile.compiler.lang.psi.NapileFile;
import org.napile.idea.plugin.module.ModuleAnalyzerUtil;
import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;

/**
 * @author Evgeny Gerashchenko
 * @since 07.05.12
 */
public class NapileTypeDeclarationProvider extends TypeDeclarationProvider
{
	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement[] getSymbolTypeDeclarations(@NotNull PsiElement symbol, @Nullable Editor editor, int offset)
	{
		if(symbol instanceof NapileElement && symbol.getContainingFile() instanceof NapileFile)
		{
			BindingTrace bindingContext = ModuleAnalyzerUtil.lastAnalyze((NapileFile) symbol.getContainingFile()).getBindingTrace();
			DeclarationDescriptor descriptor = bindingContext.get(BindingTraceKeys.DECLARATION_TO_DESCRIPTOR, symbol);
			if(descriptor instanceof CallableDescriptor)
			{
				NapileType type = ((CallableDescriptor) descriptor).getReturnType();
				if(type != null)
				{
					ClassifierDescriptor classifierDescriptor = type.getConstructor().getDeclarationDescriptor();
					if(classifierDescriptor != null)
					{
						PsiElement typeElement = BindingTraceUtil.descriptorToDeclaration(bindingContext, classifierDescriptor);
						if(typeElement != null)
						{
							return new PsiElement[]{typeElement};
						}
					}
				}
			}
		}
		return new PsiElement[0];
	}
}
