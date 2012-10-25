package org.overture.alloy;

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
		return ("pred " + name + "(" + arguments + ")\n{"+body+"}\n");
	}
}
