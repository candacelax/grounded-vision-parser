//is :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
//walking :- (S\NP) : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (walk:<e,t> $1))))
//away from :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) (away:<e,t> $2) ($1 $2)))))
//is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $2 $4) ($1 $2 $3)))))))

//away from :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e (lambda $2:<e,t> (and:<t*,t> ($0 $1) ($2 $1)))))
//away :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
//from :- ((S\NP)\(S\NP))/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (from:<e,<e,t>> $2 $3))))))
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
man :- N : (person:<e,t>)
walking :- S\NP : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (walk:<e,t> $1))))
away from :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (leave:<e,<e,t>> $2 $3))))))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<e,t> $1))))
chair :- N : (chair:<e,t>)
