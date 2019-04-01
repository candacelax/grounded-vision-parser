the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
chair :- N : (chair:<e,t>)
and :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3))))))
backpack :- N : (bag:<e,t>)
		//are :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
are :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2))))))
green :- NP : (green:<e,t>)
	       
