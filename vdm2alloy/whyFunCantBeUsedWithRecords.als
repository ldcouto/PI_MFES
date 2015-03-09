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
