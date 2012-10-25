package org.overture.alloy;

public class AlloyExp
{
	public String exp;

	public AlloyExp()
	{
	}

	public AlloyExp(String exp)
	{
		this.exp = exp;
	}
	
	@Override
	public String toString()
	{
		return exp;
	}
}