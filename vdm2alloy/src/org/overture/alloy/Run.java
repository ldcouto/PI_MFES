package org.overture.alloy;

public class Run extends Part
{
	String name;

	public Run(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return "run "+name;
	}
}
