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
