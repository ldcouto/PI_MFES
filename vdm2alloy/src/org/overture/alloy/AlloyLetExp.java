package org.overture.alloy;

import java.util.HashMap;
import java.util.Map;

import org.overture.ast.types.PType;

public class AlloyLetExp extends AlloyExp
{
	Map<String,PType> variables = new HashMap<String, PType>();
	public AlloyLetExp()
	{
	}

	public AlloyLetExp(String exp, Map<String, PType> variables)
	{
		super.exp = exp;
		this.variables.putAll(variables);
	}
}