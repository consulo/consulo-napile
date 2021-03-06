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

package org.napile.idea.plugin.projectView;

import java.util.Collection;
import java.util.Collections;

import org.napile.compiler.lang.psi.NapileCallParameterAsReference;
import org.napile.compiler.lang.psi.NapileCallParameterAsVariable;
import org.napile.compiler.lang.psi.NapileConstructor;
import org.napile.compiler.lang.psi.NapileDeclaration;
import org.napile.compiler.lang.psi.NapileElement;
import org.napile.compiler.lang.psi.NapileMethod;
import org.napile.compiler.lang.psi.NapileTypeReference;
import org.napile.compiler.lang.psi.NapileVariable;
import org.napile.idea.plugin.formatter.NapileCodeStyleSettings;
import org.napile.idea.plugin.util.IdePsiUtil;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.AbstractPsiBasedNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;

/**
 * User: Alefas
 * Date: 15.02.12
 */
public class NapileDeclarationTreeNode extends AbstractPsiBasedNode<NapileDeclaration>
{
	protected NapileDeclarationTreeNode(Project project, NapileDeclaration jetDeclaration, ViewSettings viewSettings)
	{
		super(project, jetDeclaration, viewSettings);
	}

	@Override
	public int getWeight()
	{
		PsiElement element = getValue();
		if(element instanceof NapileVariable)
			return 30;
		if(element instanceof NapileConstructor)
			return 40;
		return 50;
	}

	@Override
	protected PsiElement extractPsiFromValue()
	{
		return getValue();
	}

	@Override
	protected Collection<AbstractTreeNode> getChildrenImpl()
	{
		return Collections.emptyList();
	}

	@Override
	protected void updateImpl(PresentationData data)
	{
		NapileDeclaration declaration = getValue();
		if(declaration != null)
		{
			String text = declaration.getName();
			if(text == null)
				return;
			NapileCodeStyleSettings settings = CodeStyleSettingsManager.getInstance(getProject()).getCurrentSettings().getCustomSettings(NapileCodeStyleSettings.class);
			if(declaration instanceof NapileVariable)
			{
				NapileVariable property = (NapileVariable) declaration;
				NapileTypeReference ref = property.getType();
				if(ref != null)
				{
					if(settings.SPACE_BEFORE_TYPE_COLON)
						text += " ";
					text += ":";
					if(settings.SPACE_AFTER_TYPE_COLON)
						text += " ";
					text += ref.getText();
				}
			}
			else if(declaration instanceof NapileMethod)
			{
				NapileMethod function = (NapileMethod) declaration;
				text += "(";
				NapileElement[] parameters = function.getCallParameters();
				for(NapileElement parameter : parameters)
				{
					if(parameter instanceof NapileCallParameterAsVariable)
					{
						if(parameter.getName() != null)
						{
							text += parameter.getName();
							if(settings.SPACE_BEFORE_TYPE_COLON)
								text += " ";
							text += ":";
							if(settings.SPACE_AFTER_TYPE_COLON)
								text += " ";
						}
						NapileTypeReference typeReference = ((NapileCallParameterAsVariable) parameter).getTypeReference();
						if(typeReference != null)
						{
							text += typeReference.getText();
						}
					}
					else if(parameter instanceof NapileCallParameterAsReference)
						text += parameter.getText();
					text += ", ";
				}
				if(parameters.length > 0)
					text = text.substring(0, text.length() - 2);
				text += ")";
				NapileTypeReference typeReference = function.getReturnTypeRef();
				if(typeReference != null)
				{
					if(settings.SPACE_BEFORE_TYPE_COLON)
						text += " ";
					text += ":";
					if(settings.SPACE_AFTER_TYPE_COLON)
						text += " ";
					text += typeReference.getText();
				}
			}
			else if(declaration instanceof NapileConstructor)
			{
				NapileConstructor function = (NapileConstructor) declaration;
				text += "(";
				NapileElement[] parameters = function.getCallParameters();
				for(NapileElement parameter : parameters)
				{
					if(parameter instanceof NapileCallParameterAsVariable)
					{
						if(parameter.getName() != null)
						{
							text += parameter.getName();
							if(settings.SPACE_BEFORE_TYPE_COLON)
								text += " ";
							text += ":";
							if(settings.SPACE_AFTER_TYPE_COLON)
								text += " ";
						}
						NapileTypeReference typeReference = ((NapileCallParameterAsVariable) parameter).getTypeReference();
						if(typeReference != null)
						{
							text += typeReference.getText();
						}
					}
					else if(parameter instanceof NapileCallParameterAsReference)
						text += parameter.getText();
					text += ", ";
				}
				if(parameters.length > 0)
					text = text.substring(0, text.length() - 2);
				text += ")";
			}

			data.setPresentableText(text);
		}
	}

	@Override
	protected boolean isDeprecated()
	{
		return IdePsiUtil.isDeprecated(getValue());
	}
}
