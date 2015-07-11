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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.overture.alloy.Alloy2VdmAnalysis;
import org.overture.alloy.Formatter;

public class Sig extends Part
{
	public static class FieldType
	{
		public String sigTypeName;

		public enum Prefix
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
            if(name.equals("bool")){name="Bool";}
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
		
		public int size()
		{
			return 1;
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
		
		@Override
		public int size()
		{
			return super.size() + to.size();
		}
	}



	private final Map<String, Sig.FieldType> fields = new HashMap<String, Sig.FieldType>();
	private final List<String> fieldNames = new Vector<String>();
	public final String name;
    public boolean isFact = false;
    public boolean isOne = false;
	public boolean isWrapper = false;
    public boolean inUniv = false;
    public boolean isRecordType = false;//var to fix inv in record types..if  true  isn't print the vector constraints because is a fact.
	private List<String> quotes = new Vector<String>();
    private List<String> qts = new Vector<String>();
	public final List<String> constraints = new Vector<String>();
	public final List<Sig> supers = new Vector<Sig>();

	public Sig(String typeName)
	{
		this.name = typeName;
	}

    public void setRecordType(boolean isRecordType) {
        this.isRecordType = isRecordType;
    }

    public Sig(String name,boolean inUniv){
        this.name=name;
        this.inUniv=inUniv;
    }
    public Sig(String name,boolean inUniv,boolean fact){
        this.name=name;
        this.inUniv=inUniv;
        this.isFact=fact;
    }



    public List getQuotes(){
        return quotes;
    }

	public void addField(String name, Sig.FieldType type)
	{
		this.fieldNames.add(name);
		this.fields.put(name, type);
	}

	public FieldType getField(String name)
	{
		FieldType ft =  this.fields.get(name);
		if(ft !=null)
		{
			return ft;
		}
		
		for (Sig s : supers)
		{
			ft = s.getField(name);
			if(ft !=null)
			{
				break;
			}
		}
		return ft;
	}

	public List<String> getFieldNames()
	{
		if(supers.isEmpty())
		{
			return this.fieldNames;
		}
		
		List<String> fields = new Vector<String>();
		for (Sig s : this.supers)
		{
			fields.addAll(s.getFieldNames());
		}
		return fields;
	}

	@Override
	public String toString()
	{
        String tmp;
        if(this.inUniv){tmp="sig "+name+" in univ{}";}
        else {
            tmp = (isOne ? "one " : "")
                    + "sig "
                    + name
                    + (this.quotes.isEmpty() ? "" : " in "
                    + Alloy2VdmAnalysis.toList(quotes, "+")) + (this.supers.isEmpty() ? "" : " extends "
                    + Alloy2VdmAnalysis.toList(getNames(supers), "+")) + "{";
            //System.out.println( tmp);
            for (Entry<String, FieldType> entry : this.fields.entrySet()) {
                tmp += "\n\t" + entry.getKey() + ": " + entry.getValue() + ", ";
            }
            if (!this.fields.isEmpty()) {
                tmp = tmp.substring(0, tmp.length() - 2);
                tmp += "\n";
            }
            tmp += "}";
            if (!constraints.isEmpty() && !this.isRecordType) {
                tmp += "{";
                for (Iterator<String> itr = constraints.iterator(); itr.hasNext(); ) {
                    tmp += Formatter.format(0, itr.next());
                    if (itr.hasNext()) {
                        tmp += " and";
                    }
                }
                tmp += "}\n";
            }
        }
		return tmp;

	}

	private static List<String> getNames(List<Sig> supers2)
	{
		List<String> names = new Vector<String>();
		for (Sig s : supers2)
		{
			names.add((s==null?"null":s.name));
		}
		return names;
	}

	public void setInTypes(List<String> quotes)
	{
		this.quotes.clear();
		this.quotes.addAll(quotes);
	}
}