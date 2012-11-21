open traffic

assert cycle {
	all p : Path, k,k',k'',k''' : Kernel |
		k.lights[p] = GREEN and
			ToAmber[p,k,k'] and ToRed[p,k',k''] and ToGreen[p,k'',k'''] implies k'''.lights[p] = GREEN
}
check cycle for 8 but 1 Path
