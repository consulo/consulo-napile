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

package org.napile.idea.plugin.stubindex;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapileClass;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author Nikolay Krasko
 */
public class NapileFullClassNameIndex extends StringStubIndexExtension<NapileClass>
{
	private static final NapileFullClassNameIndex ourInstance = new NapileFullClassNameIndex();

	public static NapileFullClassNameIndex getInstance()
	{
		return ourInstance;
	}

	@NotNull
	@Override
	public StubIndexKey<String, NapileClass> getKey()
	{
		return NapileIndexKeys.FQN_KEY;
	}

	@Override
	public Collection<NapileClass> get(final String fqName, final Project project, @NotNull final GlobalSearchScope scope)
	{
		return super.get(fqName, project, new NapileSourceFilterScope(scope));
	}
}
