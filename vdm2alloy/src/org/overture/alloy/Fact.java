package org.overture.alloy;

public class Fact extends Part
{
	String name;
	String body;

	public Fact(String name, String body)
	{
		this.name = name;
		this.body = body;
	}
	
	@Override
	public String toString()
	{
		return ("fact " + name + "{\n"+body+"}\n");
	}
}
