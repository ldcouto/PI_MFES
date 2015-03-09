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
import java.util.Map.Entry;

import org.overture.alloy.ast.Sig;
import org.overture.ast.types.PType;
import org.overture.typechecker.assistant.TypeCheckerAssistantFactory;

public class Context
{
	private final Map<PType, Sig> types = new HashMap<PType, Sig>();
	private final Map<String, PType> variables = new HashMap<String, PType>();
	private final Map<String, String> stateMap = new HashMap<String, String>();
	private final Context outer;
	

	public Context()
	{
		this(null);
	}

	public Context(Context outer)
	{
		this.outer=outer;
	}
	
	

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("Types:\n");
		for (Entry<PType, Sig> entry : types.entrySet())
		{
			sb.append(entry.getKey().toString() + " -> "
					+ entry.getValue().name + "\n");
		}
		sb.append("Variables:\n");
		for (Entry<String, PType> entry : variables.entrySet())
		{
			sb.append(entry.getKey().toString() + " -> "
					+ (entry.getValue()!=null?entry.getValue().toString().replace('\n', ' '):"null") + "\n");
		}
		return sb.toString();
	}

	public Sig getSig(PType type)
	{
		//Search match
		for (Entry<PType, Sig> entry : types.entrySet())
		{
			if(entry.getKey().equals(type))
			{
				return entry.getValue();
			}
		}
		
		//Search for compatible match
		for (Entry<PType, Sig> entry : types.entrySet())
		{
			if (new TypeCheckerAssistantFactory().getTypeComparator().compatible(entry.getKey(), type))
			{
				return entry.getValue();
			}
		}
		
		//No match to search outer context
		if (outer != null)
		{
			return outer.getSig(type);
		}
		
		return null;
	}

	public Sig getSig(String sigTypeName)
	{
		for (Entry<PType, Sig> entry : types.entrySet())
		{
			if (entry.getValue().name.equals(sigTypeName))
			{
				return entry.getValue();
			}
		}

		if (outer != null)
		{
			return outer.getSig(sigTypeName);
		}
		return null;
	}

	public void merge(Context ctxt)
	{
		if (ctxt != null)
		{
			this.types.putAll(ctxt.types);
		}
	}

	public void flatten()
	{
		Context ctxt = this;
		while (ctxt.outer != null)
		{
			ctxt.outer.merge(ctxt);
		}
	}

	public void addType(PType type, Sig sig)
	{
		this.types.put(type, sig);
	}

	public void clearState()
	{
		this.stateMap.clear();
	}

	public void clearAllState()
	{
		this.stateMap.clear();
		if (outer != null)
		{
			outer.clearState();
		}
	}

	public void addState(String tag, String tag2)
	{
		this.stateMap.put(tag, tag2);
	}

	public void addVariable(String string, PType pType)
	{
		this.variables.put(string, pType);
	}
	
	public void addVariables(Map<String, PType> variables)
	{
		this.variables.putAll(variables);
	}

	public boolean containsVariable(String varName)
	{
		if (this.variables.containsKey(varName))
		{
			return true;
		} else if (outer != null)
		{
			return outer.containsVariable(varName);
		}
		return false;
	}

	public boolean containsState(String name)
	{
		if (this.stateMap.containsKey(name))
		{
			return true;
		} else if (outer != null)
		{
			return outer.containsState(name);
		}
		return false;
	}

	public String getState(String name)
	{
		String state = this.stateMap.get(name);
		if (state != null)
		{
			return state;
		} else if (outer != null)
		{
			return outer.getState(name);
		}
		return null;
	}

}
