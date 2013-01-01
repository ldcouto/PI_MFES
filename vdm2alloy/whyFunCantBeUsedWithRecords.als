sig A,B,D{}
sig C{
x : A,
y: B
}
fun f(a:A,b:B): C
{
{c:C| some b : B |c.x=a and c.y = b}
}

run {
some a:A,b:B | no f[a,b]
}

pred ff[a:A,b:B,c:C]
{
c.x=a and c.y = b
}

run {
some a:A,b:B,c:C |  ff[a,b,c] and c.x !=a and c.y !=b
}
