package org.overture.alloy.ast;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;


public class ModuleHeader extends Part
{
	String name;
	List<String> opens = new Vector<String>();

	public ModuleHeader(String name, String... opens)
	{
		this.name = name;
		this.opens.addAll(Arrays.asList(opens));
	}
	
	@Override
	public String toString()
	{
		String tmp ="module " + name + "\n\n";
		for (String op : opens)
		{
			tmp+="open "+op+"\n";
		}
		return tmp;
	}
}
