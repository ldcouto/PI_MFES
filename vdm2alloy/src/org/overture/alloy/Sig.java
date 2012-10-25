package org.overture.alloy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

public class Sig
{
	public static class FieldType
	{
		String sigTypeName;

		enum Prefix
		{
			one, set, seq, lone,undefined;
			
			public String displayName()
			{
				return (this==undefined?"":this.name());
			}
		};

		Prefix typePrefix = Prefix.one;

		public FieldType(String name)
		{
			this.sigTypeName = name;
		}

		public FieldType(String name, Prefix p)
		{
			this.sigTypeName = name;
			this.typePrefix = p;
		}

		@Override
		public String toString()
		{
			return (this.typePrefix.displayName() + " " + sigTypeName).trim();
		}
	}

	public static class MapFieldType extends FieldType
	{
		public FieldType to;

		public MapFieldType(String name, FieldType to)
		{
			super(name);
			this.to = to;
		}

		public MapFieldType(String name, Prefix p, FieldType to)
		{
			super(name, p);
			this.to = to;
		}

		@Override
		public String toString()
		{
			return sigTypeName + " " + typePrefix.displayName() + " -> " + to;
		}
	}

	private final Map<String, Sig.FieldType> fields = new HashMap<String, Sig.FieldType>();
	final List<String> fieldNames = new Vector<String>();
	public final String name;
	public boolean isOne = false;
	private List<String> quotes = new Vector<String>();
	public final List<String> constraints = new Vector<String>();

	public Sig(String typeName)
	{
		this.name = typeName;
	}

	public void addField(String name, Sig.FieldType type)
	{
		this.fieldNames.add(name);
		this.fields.put(name, type);
	}

	public FieldType getField(String name)
	{
		return this.fields.get(name);
	}

	@Override
	public String toString()
	{
		String tmp = (isOne ? "one " : "")
				+ "sig "
				+ name
				+ (this.quotes.isEmpty() ? "" : " in "
						+ Alloy2VdmAnalysis.toList(quotes, "+")) + "{";
		for (Entry<String, FieldType> entry : this.fields.entrySet())
		{
			tmp += "\n\t" + entry.getKey() + ": " + entry.getValue() + ", ";
		}
		if (!this.fields.isEmpty())
		{
			tmp = tmp.substring(0, tmp.length() - 2);
		}
		tmp += "\n}";
		if (!constraints.isEmpty())
		{
			tmp += "{";
			for (Iterator<String> itr = constraints.iterator(); itr.hasNext();)
			{
				tmp += "\n\t(" + itr.next()+")";
				if (itr.hasNext())
				{
					tmp += " and";
				}
			}
			tmp += "\n}\n";
		}
		return tmp;
	}

	public void setInTypes(List<String> quotes)
	{
		this.quotes.clear();
		this.quotes.addAll(quotes);
	}
}