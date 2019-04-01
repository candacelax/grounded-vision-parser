both :- NP/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (two:<e,t> $1))))
guys :- N : (people:<e,t>)
set down :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (put_down:<e,<e,t>> $2 $3))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
objects :- N : (objects:<e,t>)
at the same time :- NP\NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
//set down :- S\NP : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (put_down:<e,t> $1))))
//at :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
//at :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (toward:<e,t> $1))))

//toward :- (S\NP)\(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2 $1) (toward:<e,t> $2)))))

//is :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $2 $3))))))
//standing :- S\NP : (lambda $0:e (stand:<e,t> $0))
//still :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e ($0 $1)))


			 // for use without `in red`
			 //still :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
			 //is :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))

//at :- (S\NP)\(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:<e,t> (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $3 $4) ($1 $3) ($2 $4)))))))
//at the same time :- S\S : (lambda $0:<e,<e,t>> (lambda $1:e (lambda $2:e ($0 $1 $2))))
at :- (S\NP)\(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:<e,t> (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $3 $4) ($1 $3) ($2 $4)))))))
//at :- (S\NP)\(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $3) (on:<e,t> $3))))))
//at the same time :- (S\NP)\(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($1 $2) ($0 $2 $3))))))

