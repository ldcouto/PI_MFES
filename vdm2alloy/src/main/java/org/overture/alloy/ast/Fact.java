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

public class Fact extends Part
{
	String name;
	String body;
    String atrb;
    boolean hasAll;

	public Fact(String name, String body)
	{
		this.name = name;
		this.body = body;
        this.hasAll=false;
	}
    public Fact(boolean isAll,String var,String type,String body){
        this.hasAll = isAll;
        this.body=body;
        this.name=type;
        this.atrb=var;
    }

    public Fact(String name ,String body, String atrb)
    {
        this.name = name;
        this.body = body;
        this.atrb=atrb;
        this.hasAll=false;
    }


    @Override
	public String toString()
	{
        if(this.hasAll){
            return("fact "+name+"RType"+"{\n"+"all "+atrb+" : "+name+" | "+body+"\n}");
        }
       else{
            if(this.atrb!=null) {
            return ("fact " + name + "{\n" + atrb + " = { y : " + body + "}\n}\n");
        }
        else{
                return ("fact " + name + "{\n" + body + "\n}\n");
            }
        }
    }

}
