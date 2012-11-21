package org.overture.alloy;

import java.util.HashMap;
import java.util.Map;

import org.overture.ast.types.PType;

public class PredicatContext extends Context
{
	private final Map<String, PType> returnValue = new HashMap<String, PType>();

	public PredicatContext(Context outer, String returnName,PType returnType)
	{
		super(outer);
		this.returnValue.put(returnName, returnType);
	}
	
	public String getReturnName()
	{
		return this.returnValue.keySet().iterator().next();
	}
	
	public PType getReturnType()
	{
		return this.returnValue.values().iterator().next();
	}
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Return:\n"+getReturnName()+": "+getReturnType()+"\n"+super.toString();
	}
}
