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
module c

open util/relation

sig CHAR{}
sig Country{
	x: seq CHAR
}
fact CountrySet{
all c1,c2 : Country | c1.x = c2.x implies c1=c2
}

/*sig CountryCountry{
	x0: one Country, 
	x1: one Country
}*/
/*sig Relation{
	x:  (Country -> Country)
}*/

sig CountryCountry{
	x:  (Country -> Country)
}

sig Relation extends CountryCountry{}

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
	//all var0 : r.x |  let c1 = dom[var0], c2 = ran[var0] | (c1 != c2)
//forall mk_(c1,c2) in set r & c1 <> c2;
all t1, t2 : Country | t1->t2 in r.x implies (t1 != t2)

//all q : r.x | some q //(t1 != t2)
}

run isRelation
pred areNb(cn1: Country, cn2: Country, r: Relation)
{
	 /* Body */
	//((some var1 : r.x | cn1 = dom[var1] and cn2 = ran[var1]) or (some var2 : r.x | cn2 = dom[var2] and cn1 = ran[var2]))
	(cn1->cn2 in r.x) or ( cn2->cn1 in r.x)

}

run areNb
fun CountriesRel[r: Relation]: set Country
{
	 /* Body */
//	{ var3 : Country | var3 in ({var5 : Colour |  some var4 : r.x |  let c1 = dom[var4], c2 = ran[var4] | c1 + c2 = var5.x}).x}
// dunion {{c1,c2} | mk_(c1,c2) in set r};
//{ c : Country | let t = {c1,c2 : Country | c1->c2 in r.x } | c in dom[t] or c in ran[t]}
//{ c : Country | c in toSet[ {c1,c2: Country | c1->c2 in r.x }] }
 toSet[ {c1,c2: Country | c1->c2 in r.x }]
//{ c : Country | c in dom[{c1,c2 : Country | c1->c2 in r.x }] }
/*{
a->b,
c->d,
g->v,

}*/
//{ c : Country | c in {var5 : Colour | some c1,c2 : Country | c1+c2 = var5.x and c1->c2 in r.x}.x }

// dunion {{c1,c2} | mk_(c1,c2) in set r};
//{ c : Country | c in  } --dunion

//{{c1,c2} | mk_(c1,c2) in set r}
//	{c1,c2 : Country | c1->c2 in r.x } --> {c1->c2}
}

fun toSet[r : univ->univ]: set univ
{
	univ.r + r.univ
}

fun toSet[r : univ->univ->univ]: set univ
{
// 1 st + 2nd + 3rd
	(r.univ).univ + univ.(r.univ) + univ.(univ.r)
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
pred colMap(r: Relation, cols : Colouring)
{
	 /* Pre conditions */
	isRelation[r] 
	 /* Post conditions */
	(isColouring[cols]  and (isColouringOf[cols, CountriesRel[r] ]  and nbDistinctColours[cols, r] ))
}

run colMap
pred show()
{
}

run show
