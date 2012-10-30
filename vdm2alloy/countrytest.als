module m1
open country


isRelationEx : run
{
	some r : Relation | isRelation[r]
} for 3 but 0 Colouring, 0 Colour

areNbEx : run
{
	some disj c1,c2,c3 : Country, r:Relation | areNb[c1,c2,r] and not areNb[c1,c3,r]
}for 3 but 0 Colouring, 0 Colour


CountriesRelEx : run {
some r : Relation | #CountriesRel[r]>7
} for 8 but 0 Colouring, 0 Colour

sameColourEx : run
{
	some disj cn1,cn2 : Country, cols : Colouring |
		sameColour[cn1,cn2,cols]
} for 2

CountriesColEx : run{
some cols : Colouring|#CountriesCol[cols]>2
}for 3


isColouringEx : run
{
	some cols : Colouring | isColouring[cols]
} for 4

isColouringOfEx : run{
	some cols : Colouring, cns : set Country | isColouringOf[cols,cns]
}for 4

nbDistinctColoursEx : run{
 some cols : Colouring, r: Relation | nbDistinctColours[cols,r] and #r.x>2
}for 4 but 0 CHAR

colMapEx : run
{
	some r : Relation,cols : Colouring |
		colMap[r,cols] and some r.x //and one cols.x
} for 3 but 0 CHAR, 1 Relation


assert colMap
{
	all r : Relation | all cols : Colouring |
		colMap[r,cols] implies all c :CountriesRel[r] | some col : cols.x | c in col.x
} 
check colMap for  10 but 0 CHAR


test1: run{
some c : set Colouring | #c.x>2 and (all v: c.x | #v.x>1) and #CountriesCol[c]>0
}

