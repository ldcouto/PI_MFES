module m1
open c

sig A{}
fact d
{
no A //Fixes bug where Univ shows up in the 
}

isRelationEx : run
{
	some r : Relation | isRelation[r]
} for 3 but 0 Colouring, 0 Colour

areNbEx : run
{
	some disj c1,c2,c3 : Country, r:Relation | areNb[c1,c2,r] and not areNb[c1,c3,r]
}for 3 but 0 Colouring, 0 Colour//, 0 CHAR


CountriesRelEx : run {
some r,r1,r2 : Relation | #CountriesRel[r]>2 and no r1.x and some r2.x and isRelation[r] //and some r.x
} for 3 but 0 Colouring//, 0 CHAR

CountriesRelCheck : check
{
	all r : Relation | some r.x implies some CountriesRel[r]
}

sameColourEx : run
{
	some disj cn1,cn2 : Country, cols : Colouring |
		sameColour[cn1,cn2,cols]
} for 2 

CountriesColEx : run{
some cols : Colouring|#CountriesCol[cols]>2
}for 6


isColouringEx : run
{
	some cols : Colouring | isColouring[cols]
} for 6

isColouringOfEx : run{
	some cols : Colouring, cns : set Country | isColouringOf[cols,cns] and #cns>1
}for 4

nbDistinctColoursEx : run{
 some cols : Colouring, r: Relation | nbDistinctColours[cols,r] and #r.x>1 and #cols.x.x>1
}for 3


colMapEx : run
{
	some r : Relation,cols : Colouring |
		colMap[r,cols] and #r.x>2 //and one cols.x
} for 3


assert colMap
{
	all r : Relation | all cols : Colouring |
		colMap[r,cols] implies all c :CountriesRel[r] | some col : cols.x | c in col.x
} 
check colMap for  10 but 0 CHAR


test1: run{
some c : set Colouring | #c.x>2 and (all v: c.x | #v.x>1) and #CountriesCol[c]>0
}

