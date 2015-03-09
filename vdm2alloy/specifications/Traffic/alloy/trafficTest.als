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
open traffic

assert cycle {
	all p : Path, k,k',k'',k''' : Kernel |
		k.lights[p] = GREEN and
			ToAmber[p,k,k'] and ToRed[p,k',k''] and ToGreen[p,k'',k'''] implies k'''.lights[p] = GREEN
}
check cycle for 8 but 1 Path
