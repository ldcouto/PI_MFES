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
module contry

open util/relation

sig CHAR{}
sig Country{
	x: seq CHAR
}
fact CountrySet{
all c1,c2 : Country | c1.x = c2.x implies c1=c2
}

sig CountryCountry{
	x0: one Country, 
	x1: one Country
}
sig Relation{
	x: set CountryCountry
}
fact RelationSet{
all c1,c2 : Relation | c1.x = c2.x implies c1=c2
}

sig Colour{
	x: set Country
}
fact ColourSet{
all c1,c2 : Colour | c1.x = c2.x implies c1=c2
}

sig Colouring{
	x: set Colour
}
fact ColouringSet{
all c1,c2 : Colouring | c1.x = c2.x implies c1=c2
}

pred isRelation(r: Relation)
{
	 /* Body */
	all var0 : r.x |  let c1 = var0.x0, c2 = var0.x1 | (c1 != c2)
}

run isRelation
pred areNb(cn1: Country, cn2: Country, r: Relation)
{
	 /* Body */
	((some var1 : r.x | cn1 = var1.x0 and cn2 = var1.x1) or (some var2 : r.x | cn2 = var2.x0 and cn1 = var2.x1))
}

run areNb
fun CountriesRel[r: Relation]: set Country
{
	 /* Body */
	{ var3 : Country | var3 in ({var5 : Colour |  some var4 : r.x |  let c1 = var4.x0, c2 = var4.x1 | c1 in var5.x and c2 in var5.x}).x}
}

run CountriesRel
pred sameColour(cn1: Country, cn2: Country, cols: Colouring)
{
	 /* Body */
	some col : cols.x | ((cn1 in col.x) and (cn2 in col.x))
}

run sameColour
fun CountriesCol[cols: Colouring]: set Country
{
	 /* Body */
	{ var6 : Country | var6 in (cols.x).x}
}

run CountriesCol
pred isColouring(cols: Colouring)
{
	 /* Body */
	all col1, col2 : cols.x | ((col1 != col2) implies no (col1 & col2))
}

run isColouring
pred isColouringOf(cols: Colouring, cns: set Country)
{
	 /* Body */
	(CountriesCol[cols]  = cns)
}

run isColouringOf
pred nbDistinctColours(cols: Colouring, r: Relation)
{
	 /* Body */
	all cn1, cn2 : CountriesRel[r]  | (areNb[cn1, cn2, r]  implies not sameColour[cn1, cn2, cols] )
}

run nbDistinctColours
pred show()
{
}

run show
