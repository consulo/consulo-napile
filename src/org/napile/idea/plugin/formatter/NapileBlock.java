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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.NapileLanguage;
import org.napile.compiler.lang.lexer.NapileNodes;
import org.napile.compiler.lang.lexer.NapileTokens;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author yole
 * @see Block for good JavaDoc documentation
 */
public class NapileBlock extends AbstractBlock
{
	private final Indent myIndent;
	private final CodeStyleSettings mySettings;
	private final SpacingBuilder mySpacingBuilder;

	private List<Block> mySubBlocks;

	private static final TokenSet CODE_BLOCKS = TokenSet.create(NapileNodes.BLOCK, NapileNodes.CLASS_BODY, NapileNodes.ANONYM_METHOD_EXPRESSION, NapileNodes.ANONYM_METHOD);

	// private static final List<IndentWhitespaceRule>

	public NapileBlock(@NotNull ASTNode node, Alignment alignment, Indent indent, Wrap wrap, CodeStyleSettings settings, SpacingBuilder spacingBuilder)
	{

		super(node, wrap, alignment);
		myIndent = indent;
		mySettings = settings;
		mySpacingBuilder = spacingBuilder;
	}

	@Override
	public Indent getIndent()
	{
		return myIndent;
	}

	@Override
	protected List<Block> buildChildren()
	{
		if(mySubBlocks == null)
		{
			mySubBlocks = buildSubBlocks();
		}
		return new ArrayList<Block>(mySubBlocks);
	}

	private List<Block> buildSubBlocks()
	{
		List<Block> blocks = new ArrayList<Block>();

		Map<ASTNode, Alignment> childrenAlignments = createChildrenAlignments();

		for(ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext())
		{
			IElementType childType = child.getElementType();

			if(child.getTextRange().getLength() == 0)
				continue;

			if(childType == TokenType.WHITE_SPACE)
			{
				continue;
			}

			Alignment childAlignment = childrenAlignments.containsKey(child) ? childrenAlignments.get(child) : null;
			blocks.add(buildSubBlock(child, childAlignment));
		}
		return Collections.unmodifiableList(blocks);
	}

	@NotNull
	private Block buildSubBlock(@NotNull ASTNode child, Alignment childAlignment)
	{
		Wrap wrap = null;

		// Affects to spaces around operators...
		if(child.getElementType() == NapileNodes.OPERATION_REFERENCE)
		{
			ASTNode operationNode = child.getFirstChildNode();
			if(operationNode != null)
			{
				return new NapileBlock(operationNode, childAlignment, Indent.getNoneIndent(), wrap, mySettings, mySpacingBuilder);
			}
		}

		return new NapileBlock(child, childAlignment, createChildIndent(child), wrap, mySettings, mySpacingBuilder);
	}

	private static Indent indentIfNotBrace(@NotNull ASTNode child)
	{
		return child.getElementType() == NapileTokens.RBRACE || child.getElementType() == NapileTokens.LBRACE ? Indent.getNoneIndent() : Indent.getNormalIndent();
	}

	private static ASTNode getPrevWithoutWhitespace(ASTNode node)
	{
		node = node.getTreePrev();
		while(node != null && node.getElementType() == TokenType.WHITE_SPACE)
		{
			node = node.getTreePrev();
		}

		return node;
	}

	@Override
	public Spacing getSpacing(Block child1, Block child2)
	{
		return mySpacingBuilder.getSpacing(this, child1, child2);
	}

	@NotNull
	@Override
	public ChildAttributes getChildAttributes(int newChildIndex)
	{
		final IElementType type = getNode().getElementType();
		if(CODE_BLOCKS.contains(type) ||
				type == NapileNodes.WHEN ||
				type == NapileNodes.IF ||
				type == NapileNodes.FOR ||
				type == NapileNodes.WHILE ||
				type == NapileNodes.DO_WHILE)
		{

			return new ChildAttributes(Indent.getNormalIndent(), null);
		}
		else if(type == NapileNodes.DOT_QUALIFIED_EXPRESSION)
		{
			return new ChildAttributes(Indent.getContinuationWithoutFirstIndent(), null);
		}
		else if(type == NapileNodes.CALL_PARAMETER_LIST || type == NapileNodes.VALUE_ARGUMENT_LIST)
		{
			// Child index 1 - cursor is after ( - parameter alignment should be recreated
			// Child index 0 - before expression - know nothing about it
			if(newChildIndex != 1 && newChildIndex != 0 && newChildIndex < getSubBlocks().size())
			{
				Block block = getSubBlocks().get(newChildIndex);
				return new ChildAttributes(block.getIndent(), block.getAlignment());
			}
			return new ChildAttributes(Indent.getContinuationIndent(), null);
		}

		if(isIncomplete())
		{
			return super.getChildAttributes(newChildIndex);
		}

		return new ChildAttributes(Indent.getNoneIndent(), null);
	}

	@Override
	public boolean isLeaf()
	{
		return myNode.getFirstChildNode() == null;
	}

	@NotNull
	protected Map<ASTNode, Alignment> createChildrenAlignments()
	{
		CommonCodeStyleSettings jetCommonSettings = mySettings.getCommonSettings(NapileLanguage.INSTANCE);

		// Prepare default null strategy
		ASTAlignmentStrategy strategy = ASTAlignmentStrategy.getNullStrategy();

		// Redefine list of strategies for some special elements
		IElementType parentType = myNode.getElementType();
		if(parentType == NapileNodes.CALL_PARAMETER_LIST)
		{
			strategy = getAlignmentForChildInParenthesis(jetCommonSettings.ALIGN_MULTILINE_PARAMETERS, NapileNodes.CALL_PARAMETER_AS_VARIABLE, NapileTokens.COMMA, jetCommonSettings.ALIGN_MULTILINE_METHOD_BRACKETS, NapileTokens.LPAR, NapileTokens.RPAR);
		}
		else if(parentType == NapileNodes.VALUE_ARGUMENT_LIST)
		{
			strategy = getAlignmentForChildInParenthesis(jetCommonSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS, NapileNodes.VALUE_ARGUMENT, NapileTokens.COMMA, jetCommonSettings.ALIGN_MULTILINE_METHOD_BRACKETS, NapileTokens.LPAR, NapileTokens.RPAR);
		}

		// Construct information about children alignment
		HashMap<ASTNode, Alignment> result = new HashMap<ASTNode, Alignment>();

		for(ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext())
		{
			IElementType childType = child.getElementType();

			if(child.getTextRange().getLength() == 0)
				continue;

			if(childType == TokenType.WHITE_SPACE)
			{
				continue;
			}

			Alignment childAlignment = strategy.getAlignment(child);
			if(childAlignment != null)
			{
				result.put(child, childAlignment);
			}
		}

		return result;
	}

	private static ASTAlignmentStrategy getAlignmentForChildInParenthesis(boolean shouldAlignChild, final IElementType parameter, final IElementType delimiter, boolean shouldAlignParenthesis, final IElementType openParenth, final IElementType closeParenth)
	{
		// TODO: Check this approach in other situations and refactor
		final Alignment parameterAlignment = shouldAlignChild ? Alignment.createAlignment() : null;
		final Alignment parenthesisAlignment = shouldAlignParenthesis ? Alignment.createAlignment() : null;

		return new ASTAlignmentStrategy()
		{
			@Override
			public Alignment getAlignment(ASTNode node)
			{
				IElementType childNodeType = node.getElementType();

				ASTNode prev = getPrevWithoutWhitespace(node);
				if((prev != null && prev.getElementType() == TokenType.ERROR_ELEMENT) || childNodeType == TokenType.ERROR_ELEMENT)
				{
					return parameterAlignment;
				}

				if(childNodeType == openParenth || childNodeType == closeParenth)
				{
					return parenthesisAlignment;
				}

				if(childNodeType == parameter || childNodeType == delimiter)
				{
					return parameterAlignment;
				}

				return null;
			}
		};
	}

	static ASTIndentStrategy[] INDENT_RULES = new ASTIndentStrategy[]{
			ASTIndentStrategy.forNode("No indent for braces in blocks").in(NapileNodes.BLOCK, NapileNodes.CLASS_BODY, NapileNodes.ANONYM_METHOD_EXPRESSION).forType(NapileTokens.RBRACE, NapileTokens.LBRACE).set(Indent.getNoneIndent()),

			ASTIndentStrategy.forNode("Indent for block content").in(NapileNodes.BLOCK, NapileNodes.CLASS_BODY, NapileNodes.ANONYM_METHOD_EXPRESSION).notForType(NapileTokens.RBRACE, NapileTokens.LBRACE).set(Indent.getNormalIndent()),

			ASTIndentStrategy.forNode("For a single statement if 'for'").in(NapileNodes.BODY).notForType(NapileNodes.BLOCK).set(Indent.getNormalIndent()),

			ASTIndentStrategy.forNode("For the entry in when").forType(NapileNodes.WHEN_ENTRY).set(Indent.getNormalIndent()),

			ASTIndentStrategy.forNode("For single statement in THEN and ELSE").in(NapileNodes.THEN, NapileNodes.ELSE).notForType(NapileNodes.BLOCK).set(Indent.getNormalIndent()),

			ASTIndentStrategy.forNode("Indent for parts").in(NapileNodes.VARIABLE, NapileNodes.METHOD, NapileNodes.CONSTRUCTOR).set(Indent.getNoneIndent()),
	};

	@Nullable
	protected static Indent createChildIndent(@NotNull ASTNode child)
	{
		ASTNode childParent = child.getTreeParent();
		IElementType childType = child.getElementType();

		for(ASTIndentStrategy strategy : INDENT_RULES)
		{
			Indent indent = strategy.getIndent(child);
			if(indent != null)
			{
				return indent;
			}
		}

		// TODO: Try to rewrite other rules to declarative style

		if(childParent != null && childParent.getElementType() == NapileNodes.WHEN_ENTRY)
		{
			ASTNode prev = getPrevWithoutWhitespace(child);
			if(prev != null && prev.getText().equals("->"))
			{
				return indentIfNotBrace(child);
			}
		}

		if(childParent != null && childParent.getElementType() == NapileNodes.DOT_QUALIFIED_EXPRESSION)
		{
			if(childParent.getFirstChildNode() != child && childParent.getLastChildNode() != child)
			{
				return Indent.getContinuationWithoutFirstIndent(false);
			}
		}

		if(childParent != null)
		{
			IElementType parentType = childParent.getElementType();

			if(parentType == NapileNodes.CALL_PARAMETER_LIST || parentType == NapileNodes.VALUE_ARGUMENT_LIST)
			{
				ASTNode prev = getPrevWithoutWhitespace(child);
				if(childType == NapileTokens.RPAR && (prev == null || prev.getElementType() != TokenType.ERROR_ELEMENT))
				{
					return Indent.getNoneIndent();
				}

				return Indent.getContinuationWithoutFirstIndent();
			}

			if(parentType == NapileNodes.TYPE_PARAMETER_LIST || parentType == NapileNodes.TYPE_ARGUMENT_LIST)
			{
				return Indent.getContinuationWithoutFirstIndent();
			}
		}

		return Indent.getNoneIndent();
	}
}
