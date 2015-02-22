/*
 * Copyright 2010-2013 napile.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.napile.idea.moduling.dom;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapileTypeParameter;
import org.napile.idea.moduling.dom.converters.ToTypeParameterConverter;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;

/**
 * @author VISTALL
 * @since 10:50/26.01.13
 */
public interface RefAttributeToTypeParameter extends DomElement
{
	@Required
	@NotNull
	@Attribute("attribute")
	GenericAttributeValue<String> getAttribute();

	@Required
	@NotNull
	@Convert(value = ToTypeParameterConverter.class)
	@Attribute("type-parameter")
	GenericAttributeValue<NapileTypeParameter> getTypeParameter();
}
