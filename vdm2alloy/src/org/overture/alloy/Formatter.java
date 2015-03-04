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
package org.overture.alloy;

import java.util.HashSet;
import java.util.Set;

public class Formatter
{
	public static String format(int startTab, String data)
	{
		int tabs = startTab;
//		int lb = 0;
//		int rb = 0;
//		int pipes = 0;
		int brackets = 1;
		Set<Integer> s = new HashSet<Integer>(); 
		StringBuffer sb = new StringBuffer();
		
		for (char c : data.toCharArray())
		{
			if(c=='(')
			{	
				brackets++;
				tabs++;
				sb.append("\n"+getTabs(tabs));sb.append(c);
			
//				sb.append("\n"+getTabs(tabs));
				
				continue;
			}else if(c==')')
			{
				brackets--;
				sb.append(c);
				tabs--;
				sb.append("\n"+getTabs(tabs));
				continue;
			}else if( c=='|')
			{
				sb.append(c);
				tabs++;
				s.add(brackets);
//				sb.append("\n"+getTabs(tabs));
				continue;
			}
			
			if(!s.isEmpty()&& s.contains(brackets))
			{
				tabs--;
				s.remove(brackets);
			}
			
			if(c!='\n')
			sb.append(c);
			
		}

		return sb.toString();
	}
	
	static String getTabs(int tabs)
	{
		String d = "";
		for (int i = 0; i < tabs; i++)
		{
			d+="\t";
		}
		return d;
	}
}
