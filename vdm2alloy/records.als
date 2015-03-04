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
sig C{}
sig B	 { y : C}
sig A {	x : C}
sig R {
	left  : A,
	right: B
}

pred f(c: C, r : R)
{
//	r.l=(some a : A | a.x =c ) and r.r = (some b : B | b.y = c)
	some a : A | 
		a.x = c and 
		some b : B | 
			b.y = c and 
			(r.left = a and r.right = b)
}

pred f1(c: C, r : R)
{
//	r.l=(some a : A | a.x =c ) and r.r = (some b : B | b.y = c)
	let 	a = { a : A | a.x = c},
			b = { b : B |	b.y = c}
		| 
			(r.left = a and r.right = b)
}



assert f1a{
all r : R, c : C | (f[c,r] implies f[c,r] and 
											f1[c,r] and 
											(r.left).x = c and (r.right).y = c)
						and not f[c,r] implies not f1[c,r]
}
run f
run f1
check f1a


fun ff[c:C]:R
{
	{r : R | f[c,r]}
}

run {
some c : C | some ff[c]
}


fun f2[c:C]:C
{
let 	a = { a : A | a.x = c},
			b = { b : B |	b.y = c}
		| f3[a,b]
}

fun f3[a:A,b:B]:C
{
	a.x
}


run{
some c: C |some f2[c]
}
