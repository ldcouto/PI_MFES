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

public class Run extends Part
{
    private String scopeInt="";
    String name;
    String scope="";

	public Run(String name)
	{
		this.name = name;
	}
	public Run(String name,String scope){
        this.name =name;
        this.scope=scope;
    }
    public Run(String name,String scope,String scopeInt){
        this.name =name;
        this.scope=scope;
        this.scopeInt = scopeInt;
    }

	@Override
	public String toString()
	{
        if(!this.scope.equals("") && !this.scopeInt.equals("")){return "run {\nsome "+this.name+"\n} for "+this.scope+" but "+this.scopeInt+" int";}
        else {
            if(!this.scope.equals("")){
                {return "run {\nsome "+this.name+"\n} for "+this.scope;}
            }
            else{
                return "run " + name;
            }
        }
	}
}
