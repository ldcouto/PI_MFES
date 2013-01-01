package org.overture.alloy;

import java.util.HashMap;
import java.util.Map;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptor;
import org.overture.ast.expressions.AMkBasicExp;
import org.overture.ast.node.INode;

public class BasicTokenSearch extends DepthFirstAnalysisAdaptor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final Map<String,INode> mkbasicToken = new HashMap<String,INode>();

	@Override
	public void caseAMkBasicExp(AMkBasicExp node) throws AnalysisException
	{
		mkbasicToken.put(getName(node),node);
	}
	
	
	public static String getName(AMkBasicExp node)
	{
		return "TOKEN_"+node.getArg().toString().replace("\"", "").toUpperCase();
	}
}
