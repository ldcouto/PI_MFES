package org.overture.alloy.ast;


public class AlloyTypeBind extends AlloyExp
{
	public final String var;
	public final Sig type;

	public AlloyTypeBind(String var, Sig type)
	{
		super(var + " : " + type.name);
		this.var = var;
		this.type = type;
	}
}
