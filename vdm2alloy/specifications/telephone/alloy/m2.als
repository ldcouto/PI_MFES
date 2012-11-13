module m1
open telephone

pred ss{}
run ss

run{
some e: Exchange | #e.status>2
}

//Utility predicate
pred eq(e : Exchange, e': Exchange)
{
	e.status = e'.status
	e.calls = e'.calls
}

//Utility predicate
pred free(e : Exchange,  new_s : Subscriber)
{
	e.status[new_s]=FR
}



liftExample: run{
	some e,e' : Exchange, s1 : Subscriber |
	Lift[e,e',s1] and
	e.status[s1] = FR
}

assert liftClearAttempt
{
	all disj e,e',e'': Exchange, s1 : Subscriber |
		free[e,s1] and Lift[e,e',s1] and
		ClearAttempt[e',e'',s1] implies eq[e,e'']

}
check liftClearAttempt for 4 but 3 Exchange

assert liftMakeUnClearUn
{
	all disj e,e',e'',e''': Exchange, s1 : Subscriber |
		free[e,s1] and Lift[e,e',s1] and
		MakeUn[e',e'',s1] and ClearUn[e'',e''',s1] implies eq[e,e''']

}
check liftMakeUnClearUn for 4 but 3 Exchange

liftConnectClearWaitExample: run 
{
	some disj e, e',e'',e''' : Exchange, s1,s2 : Subscriber |
		free[e,s1] and Lift[e,e',s1] and
		Connect[e',e'',s1,s2] and
		ClearWait[e'',e''',s1] and eq[e,e''']
} for 3 but 4 Exchange


assert liftConnectClearWait
{
	all disj e,e',e'',e''' : Exchange, s1,s2 : Subscriber |
		free[e,s1] and Lift[e,e',s1] and
		Connect[e',e'',s1,s2] and
		ClearWait[e'',e''',s1] implies eq[e,e''']
}
check liftConnectClearWait for 4 but 4 Exchange


assert liftConnectAnswerClearSpeakClearUn
{
	all disj e,e',e'',e''',e'''',e''''' : Exchange, s1,s2 : Subscriber |
		free[e,s1] and Lift[e,e',s1] and
		Connect[e',e'',s1,s2] and 
		Answer[e'',e''',s2] and
		ClearSpeak[e''',e'''',s1] and
		ClearUn[e'''',e''''',s2] implies eq[e,e''''']
}
check liftConnectAnswerClearSpeakClearUn  for 4 but 7 Exchange

assert liftConnectAnswerSuspendClearWait
{
	all disj e,e',e'',e''',e'''',e''''' : Exchange, s1,s2 : Subscriber |
		free[e,s1] and Lift[e,e',s1] and
		Connect[e',e'',s1,s2] and 
		Answer[e'',e''',s2] and
		Suspend[e''',e'''',s2] and
		ClearWait[e'''',e''''',s1] implies eq[e,e''''']
}
check liftConnectAnswerSuspendClearWait  for 20 but 7 Exchange
