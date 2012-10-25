module test

open util/relation

sig Subscriber{}
one sig AI{}
one sig SI{}
one sig WI{}
sig Initiator in AI + SI + WI{}
one sig SR{}
one sig WR{}
sig Recipient in SR + WR{}
one sig FR{}
one sig UN{}
sig Status in FR + UN + Initiator + Recipient{}

sig Exchange{
/*	status: Subscriber -> one Status,
	calls: Subscriber -> Subscriber
*/

	status: Subscriber -> lone Status,
	calls: Subscriber lone -> lone Subscriber

}
{
(	all i : dom[calls] | (((status[i]  = WI) and (status[calls[i] ]  = WR)) 
		or ((status[i]  = SI) and (status[calls[i] ]  = SR)))
)and
(
	/*Exchange.calls is an INMAP*/
//	( all fs1,fs2 : calls.univ | calls[fs1] = calls[fs2] implies fs1=fs2 )
injective[calls,Exchange] and functional[calls,Exchange] and functional[status,Exchange]
)
}
fact{
	/*Exchange.status is a MAP wrong */
	 (some fe: Exchange | 
		( all fs1,fs2 : fe.status.univ | fs1=fs2  implies fe.status[fs1] = fe.status[fs2] ))
//	and
	/*Exchange.calls is an INMAP*/
//	 (some fe: Exchange | 
//	( all fs1,fs2 : fe.calls.univ | fe.calls[fs1] = fe.calls[fs2] implies fs1=fs2 ))
}

pred Lift(e : Exchange, e' : Exchange, s: Subscriber)
{
	 /* Frame conditions */
	e'.calls = e.calls
	 /* Pre conditions */
	(s in dom[(e.status :> (FR))])
	 /* Post conditions */
	(e'.status = (e.status ++ (s -> AI)))
}

run Lift
pred Connect(e : Exchange, e' : Exchange, i: Subscriber, r: Subscriber)
{
	 /* Pre conditions */
	((i in dom[(e.status :> (AI))]) and (r in dom[(e.status :> (FR))]))
	 /* Post conditions */
	((e'.status = (e.status ++ (i -> WI + r -> WR))) and (e'.calls = (e.calls ++ (i -> r))))
}

run Connect
pred MakeUn(e : Exchange, e' : Exchange, i: Subscriber)
{
	 /* Frame conditions */
	e'.calls = e.calls
	 /* Pre conditions */
	(i in dom[(e.status :> (AI))])
	 /* Post conditions */
	(e'.status = (e.status ++ (i -> UN)))
}

run MakeUn
pred Answer(e : Exchange, e' : Exchange, r: Subscriber)
{
	 /* Frame conditions */
	e'.calls = e.calls
	 /* Pre conditions */
	(r in dom[(e.status :> (WR))])
	 /* Post conditions */
	 /*Map domain pre condition */
	r in (~(e'.calls)).univ
	(e'.status = (e.status ++ (r -> SR + ~(e'.calls)[r]  -> SI)))
}

run Answer
pred ClearAttempt(e : Exchange, e' : Exchange, i: Subscriber)
{
	 /* Frame conditions */
	e'.calls = e.calls
	 /* Pre conditions */
	(i in dom[(e.status :> (AI))])
	 /* Post conditions */
	(e'.status = (e.status ++ (i -> FR)))
}

run ClearAttempt
pred ClearWait(e : Exchange, e' : Exchange, i: Subscriber)
{
	 /* Pre conditions */
	(i in dom[(e.status :> (WI))])
	 /* Post conditions */
	 /*Map domain pre condition */
	i in (e.calls).univ
	((e'.status = (e.status ++ (i -> FR + e.calls[i]  -> FR))) and (e'.calls = (univ -(i)) <: e.calls)) /*univ-1*/
}

run ClearWait
pred ClearSpeak(e : Exchange, e' : Exchange, i: Subscriber)
{
	 /* Pre conditions */
	(i in dom[(e.status :> (SI))])
	 /* Post conditions */
	 /*Map domain pre condition */
	i in (e.calls).univ
	((e'.status = (e.status ++ (i -> FR + e.calls[i]  -> UN))) and (e'.calls = (e.calls.univ -(i)) <: e.calls))
}

run ClearSpeak
pred Suspend(e : Exchange, e' : Exchange, r: Subscriber)
{
	 /* Frame conditions */
	e'.calls = e.calls
	 /* Pre conditions */
	(r in dom[(e.status :> (SR))])
	 /* Post conditions */
	 /*Map domain pre condition */
	r in (~(e'.calls)).univ
	(e'.status = (e.status ++ (r -> WR + ~(e'.calls)[r]  -> WI)))
}

run Suspend
pred ClearUn(e : Exchange, e' : Exchange, s: Subscriber)
{
	 /* Frame conditions */
	e'.calls = e.calls
	 /* Pre conditions */
	(s in dom[(e.status :> (UN))])
	 /* Post conditions */
	(e'.status = (e.status ++ (s -> FR)))
}

run ClearUn
pred show{}
run show
