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
