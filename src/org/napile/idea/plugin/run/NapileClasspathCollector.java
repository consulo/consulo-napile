/*
 * Copyright 2010-2013 napile.org
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

package org.napile.idea.plugin.run;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleExtensionWithSdkOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootPolicy;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 16:50/22.02.13
 */
public class NapileClasspathCollector extends RootPolicy<Object>
{
	private StringBuilder builder = new StringBuilder();

	public NapileClasspathCollector(Module module)
	{
		ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
		for(OrderEntry orderEntry : rootManager.getOrderEntries())
		{
			orderEntry.accept(this, null);
		}
	}

	@Override
	public Object visitModuleJdkOrderEntry(ModuleExtensionWithSdkOrderEntry jdkOrderEntry, Object value)
	{
		for(VirtualFile v : jdkOrderEntry.getFiles(OrderRootType.CLASSES))
		{
			builder.append(FileUtil.toSystemIndependentName(v.getPresentableUrl())).append(";");
		}

		return null;
	}

	@Override
	public Object visitLibraryOrderEntry(LibraryOrderEntry libraryOrderEntry, Object value)
	{
		for(VirtualFile v : libraryOrderEntry.getFiles(OrderRootType.CLASSES))
		{
			builder.append(FileUtil.toSystemIndependentName(v.getPresentableUrl())).append(";");
		}

		return null;
	}

	public String getClasspath()
	{
		return builder.toString();
	}
}
