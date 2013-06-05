package org.overture.alloy.ast;

import org.overture.alloy.Formatter;

public class Pred extends Part
{
	String name;
	String arguments;
	String body;

	public Pred(String name, String arguments, String body)
	{
		this.name = name;
		this.arguments = arguments;
		this.body = body;
	}
	
	@Override
	public String toString()
	{
		return ("pred " + name + "(" + arguments + ")\n{"+Formatter.format(0, body)+"\n}\n");
	}
}
