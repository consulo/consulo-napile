/*
 * Copyright 2010-2012 napile.org
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

package org.napile.idea.plugin.stubindex;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapileNamedMethodOrMacro;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author Nikolay Krasko
 */
public class NapileShortMacroNameIndex extends StringStubIndexExtension<NapileNamedMethodOrMacro>
{
	private static final NapileShortMacroNameIndex ourInstance = new NapileShortMacroNameIndex();

	public static NapileShortMacroNameIndex getInstance()
	{
		return ourInstance;
	}

	@NotNull
	@Override
	public StubIndexKey<String, NapileNamedMethodOrMacro> getKey()
	{
		return NapileIndexKeys.MACROS_SHORT_NAME_KEY;
	}

	@Override
	public Collection<NapileNamedMethodOrMacro> get(final String s, final Project project, @NotNull final GlobalSearchScope scope)
	{
		return super.get(s, project, new NapileSourceFilterScope(scope));
	}
}
