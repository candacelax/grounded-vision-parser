the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
chair :- N : (chair:<e,t>)
is :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
green :- NP : (green:<e,t>)
