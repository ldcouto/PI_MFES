/*
 * Copyright (C) 2012 Kenneth Lausdahl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */
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
