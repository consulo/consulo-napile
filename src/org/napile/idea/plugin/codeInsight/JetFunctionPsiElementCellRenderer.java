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

import org.napile.compiler.lang.descriptors.SimpleMethodDescriptor;
import org.napile.compiler.lang.psi.NapileNamedFunction;
import org.napile.compiler.lang.resolve.BindingContext;
import org.napile.compiler.resolve.DescriptorRenderer;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.psi.PsiElement;

/**
 * @author Evgeny Gerashchenko
 * @since 06.05.12
 */
public class JetFunctionPsiElementCellRenderer extends DefaultPsiElementCellRenderer
{
	private final BindingContext bindingContext;

	public JetFunctionPsiElementCellRenderer(BindingContext bindingContext)
	{
		this.bindingContext = bindingContext;
	}

	@Override
	public String getElementText(PsiElement element)
	{
		if(element instanceof NapileNamedFunction)
		{
			NapileNamedFunction function = (NapileNamedFunction) element;
			SimpleMethodDescriptor fd = bindingContext.get(BindingContext.FUNCTION, function);
			assert fd != null;
			return DescriptorRenderer.TEXT.render(fd);
		}
		return super.getElementText(element);
	}
}
