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
module country

open util/relation
open vdmutil

sig CHAR{}
sig Country{
	x: seq CHAR
}
fact CountrySet{
all c1,c2 : Country | c1.x = c2.x implies c1=c2
}

sig CountryCountry{
	x: Country  -> Country
}
sig Relation extends CountryCountry{}
fact RelationInv{
all r : Relation | isRelation[r] 
}

fact ColourSet{
all c1,c2 : Colour | c1.x = c2.x implies c1=c2
}

sig Colour{
	x: set Country
}
fact ColouringSet{
all c1,c2 : Colouring | c1.x = c2.x implies c1=c2
}

sig Colouring{
	x: set Colour
}
pred isRelation(r: Relation)
{	 /* Body */	
	( all c1 : Country, c2 : Country | 
		(c1->c2 in r.x implies 
			(c1 != c2)
		)
	)

}

run isRelation
pred areNb(cn1: Country, cn2: Country, r: Relation)
{	 /* Body */	
	(
		(cn1 -> cn2  in r.x)
	 or 
		(cn2 -> cn1  in r.x)
	)

}

run areNb
fun CountriesRel[r: Relation]: set Country
{	 /* Body */	toSet[ {c1 : Country, c2 : Country | c1->c2 in r.x}]
}

run CountriesRel
pred sameColour(cn1: Country, cn2: Country, cols: Colouring)
{	 /* Body */	some col : cols.x | 
	(
		(cn1 in col.x)
	 and 
		(cn2 in col.x)
	)

}

run sameColour
fun CountriesCol[cols: Colouring]: set Country
{	 /* Body */	
	(cols.x)
.x
}

run CountriesCol
pred isColouring(cols: Colouring)
{	 /* Body */	
	( all col1, col2 : cols.x | 
		(
			(col1 != col2)
		 implies no 
			(col1 & col2)
		)
	)

}

run isColouring
pred isColouringOf(cols: Colouring, cns: set Country)
{	 /* Body */	
	(CountriesCol[cols]  = cns)

}

run isColouringOf
pred nbDistinctColours(cols: Colouring, r: Relation)
{	 /* Body */	
	( all cn1, cn2 : CountriesRel[r]  | 
		(areNb[cn1, cn2, r]  implies not sameColour[cn1, cn2, cols] )
	)

}

run nbDistinctColours
pred colMap(r: Relation, cols : Colouring)
{	 /* Pre conditions */	isRelation[r] 	 /* Post conditions */	
	(isColouring[cols]  and 
		(isColouringOf[cols, CountriesRel[r] ]  and nbDistinctColours[cols, r] )
	)

}

run colMap
pred show()
{
}

run show
