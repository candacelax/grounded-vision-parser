the:- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<e,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (in:<e,<e,t>> $2 $3))))))
red :- NP : (lambda $0:e (and:<t*,t> (red:<e,t> $0) (shirt:<e,t> $0)))
is :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $2 $3))))))
standing :- S\NP : (lambda $0:e (stand:<e,t> $0))
very :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
still :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e ($0 $1)))


			 // for use without `in red`
			 //still :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
			 //is :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
