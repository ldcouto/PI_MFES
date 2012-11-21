package org.overture.alloy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.overture.ast.types.PType;
import org.overture.typechecker.TypeComparator;

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
			if (TypeComparator.compatible(entry.getKey(), type))
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
