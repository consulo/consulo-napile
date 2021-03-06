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

package org.napile.idea.plugin.formatter;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.NapileLanguage;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;

/**
 * @author Nikolay Krasko
 */
public class NapileLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider
{
	@NotNull
	@Override
	public Language getLanguage()
	{
		return NapileLanguage.INSTANCE;
	}

	@Override
	public String getCodeSample(@NotNull SettingsType settingsType)
	{
		switch(settingsType)
		{
			case WRAPPING_AND_BRACES_SETTINGS:
				return CodeStyleAbstractPanel.readFromFile(NapileLanguageCodeStyleSettingsProvider.class, "wrapping.ns.preview");
			default:
				return CodeStyleAbstractPanel.readFromFile(NapileLanguageCodeStyleSettingsProvider.class, "default.ns.preview");
		}
	}

	@Override
	public String getLanguageName()
	{
		return NapileLanguage.NAME;
	}

	@Override
	public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType)
	{
		switch(settingsType)
		{
			case SPACING_SETTINGS:
				consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS", "SPACE_AROUND_LOGICAL_OPERATORS", "SPACE_AROUND_EQUALITY_OPERATORS", "SPACE_AROUND_RELATIONAL_OPERATORS", "SPACE_AROUND_ADDITIVE_OPERATORS", "SPACE_AROUND_MULTIPLICATIVE_OPERATORS", "SPACE_AROUND_UNARY_OPERATOR", "SPACE_AFTER_COMMA", "SPACE_BEFORE_COMMA");

				consumer.showCustomOption(NapileCodeStyleSettings.class, "SPACE_AROUND_RANGE", "Around range (..)", CodeStyleSettingsCustomizable.SPACES_AROUND_OPERATORS);

				consumer.showCustomOption(NapileCodeStyleSettings.class, "SPACE_AFTER_TYPE_COLON", "Space after colon, before declarations' type", CodeStyleSettingsCustomizable.SPACES_OTHER);

				consumer.showCustomOption(NapileCodeStyleSettings.class, "SPACE_BEFORE_TYPE_COLON", "Space before colon, after declarations' name", CodeStyleSettingsCustomizable.SPACES_OTHER);

				consumer.showCustomOption(NapileCodeStyleSettings.class, "SPACE_AFTER_EXTEND_COLON", "Space after colon in new type definition", CodeStyleSettingsCustomizable.SPACES_OTHER);

				consumer.showCustomOption(NapileCodeStyleSettings.class, "SPACE_BEFORE_EXTEND_COLON", "Space before colon in new type definition", CodeStyleSettingsCustomizable.SPACES_OTHER);
				break;
			case WRAPPING_AND_BRACES_SETTINGS:
				consumer.showStandardOptions(
						// "ALIGN_MULTILINE_CHAINED_METHODS",
						"ALIGN_MULTILINE_PARAMETERS", "ALIGN_MULTILINE_PARAMETERS_IN_CALLS", "ALIGN_MULTILINE_METHOD_BRACKETS");
				break;
			default:
				consumer.showStandardOptions();
		}
	}

	@Override
	public IndentOptionsEditor getIndentOptionsEditor()
	{
		return new SmartIndentOptionsEditor();
	}

	@Override
	public CommonCodeStyleSettings getDefaultCommonSettings()
	{
		CommonCodeStyleSettings commonCodeStyleSettings = new CommonCodeStyleSettings(getLanguage());
		commonCodeStyleSettings.initIndentOptions();
		return commonCodeStyleSettings;
	}
}
