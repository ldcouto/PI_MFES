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

import java.util.HashMap;
import java.util.Map;

import org.overture.ast.types.PType;

public class PredicatContext extends Context
{
	private final Map<String, PType> returnValue = new HashMap<String, PType>();

	public PredicatContext(Context outer, String returnName,PType returnType)
	{
		super(outer);
		this.returnValue.put(returnName, returnType);
	}
	
	public String getReturnName()
	{
		return this.returnValue.keySet().iterator().next();
	}
	
	public PType getReturnType()
	{
		return this.returnValue.values().iterator().next();
	}
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "Return:\n"+getReturnName()+": "+getReturnType()+"\n"+super.toString();
	}
}
