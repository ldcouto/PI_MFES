module vdmutil

fun toSet[r : univ->univ]: set univ
{
	univ.r + r.univ
}

fun toSet[r : univ->univ->univ]: set univ
{
// 1 st + 2nd + 3rd
	(r.univ).univ + univ.(r.univ) + univ.(univ.r)
}
