package org.overture.alloy.ast;

import org.overture.alloy.Formatter;

public class Fun extends Part
{
	String name;
	String arguments;
	String body;
	private String returnType;

	public Fun(String name, String arguments, String body, String returnType)
	{
		this.name = name;
		this.arguments = arguments;
		this.body = body;
		this.returnType= returnType;
	}
	
	@Override
	public String toString()
	{
		return ("fun " + name + "[" + arguments + "]: "+returnType+"\n{"+Formatter.format(0, body)+"\n}\n");
	}
}
