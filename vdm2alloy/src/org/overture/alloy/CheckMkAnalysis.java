/*
 * Copyright (C) 2012 Kenneth Lausdahl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */
package org.overture.alloy;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptorAnswer;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.expressions.AMkBasicExp;
import org.overture.ast.expressions.AMkTypeExp;
import org.overture.ast.node.INode;

public class CheckMkAnalysis extends DepthFirstAnalysisAdaptorAnswer<Boolean>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Boolean caseAExplicitFunctionDefinition(
			AExplicitFunctionDefinition node) throws AnalysisException
	{
		Boolean ret = createNewReturnValue(node);

		ret = mergeReturns(ret, node.getBody().apply(this));
		if (node.getPrecondition() != null)
		{
		ret=	mergeReturns(ret, node.getPrecondition().apply(this));
		}
		if (node.getPostcondition() != null)
		{
			ret=mergeReturns(ret, node.getPostcondition().apply(this));
		}

		return ret;
	}
	
	@Override
	public Boolean caseAMkBasicExp(AMkBasicExp node) throws AnalysisException
	{
		return true;
	}
	
	@Override
	public Boolean caseAMkTypeExp(AMkTypeExp node) throws AnalysisException
	{
		return true;
	}

	@Override
	public Boolean mergeReturns(Boolean original, Boolean new_)
	{
		return (original != null ? original : false)
				|| (new_ != null ? new_ : false);
	}

	@Override
	public Boolean createNewReturnValue(INode node)
	{
		return false;
	}

	@Override
	public Boolean createNewReturnValue(Object node)
	{
		return false;
	}

}
