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
module m1
open CountryColouring




sig A{}
fact d
{
no A //Fixes bug where Univ shows up in the
}

isRelationEx : run
{
	some r : Relation | isRelation[r]
} for 3 but 0 setOfColour, 0 setOfCountry

areNbEx : run
{
	some disj c1,c2,c3 : Country, r:Relation | areNb[c1,c2,r] and not areNb[c1,c3,r]
}for 3 but 0 setOfColour, 0 setOfCountry


CountriesRelEx : run {
some r : Relation | #CountriesRel[r]>2
} for 3 but 0 setOfColour

sameColourEx : run
{
	some disj cn1,cn2 : Country, cols : Colouring|
		sameColour[cn1,cn2,cols]
} for 2

CountriesColEx : run{
some cols : Colouring | #CountriesCol[cols]>2
}for 4


isColouringEx : run
{
	some cols : Colouring | isColouring[cols]
} for 6

isColouringOfEx : run{
	some cols : Colouring, cns : set Country | isColouringOf[cols,cns] and #cns>1
}for 4


colMapEx : run
{
	some r : Relation,cols : Colouring |
		colMap[r,cols] and some r.contentsCountryCountry //and one cols.x
} for 3 but  1 setOfCountryCountry




dd : run {
 some cols : Colouring, r: Relation | some cols.contentsColour.contentsCountry and
	all cn1, cn2 : CountriesRel[r]  | (areNb[cn1, cn2, r]  and not sameColour[cn1, cn2, cols] )
}

nbDistinctColoursEx : run{
 some cols : Colouring, r: Relation | nbDistinctColours[cols,r] and #r.contentsCountryCountry>2 and #cols.contentsColour>0 and some  cols.contentsColour  and isColouringOf[cols,CountriesRel[r]] and (some  cn1, cn2 : CountriesRel[r]  | areNb[cn1, cn2, r] )
}for 8






assert colMap
{
	all r : Relation | all cols : Colouring |
		colMap[r,cols.contentsColour] implies all c :CountriesRel[r] | some col : cols.contentsColour | c in col.contentsColour
}
check colMap for  5


test1: run{
some c : set Colouring | #c.contentsColour>2 and (all v: c.contentsColour | #v.contentsCountry>1) and #CountriesCol[c]>0
}

