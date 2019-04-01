// GENERAL
male :- N : (person:<e,t>)

// SENTENCE 1
another :- NP\N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<e,t>)
watches :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $2) ($1 $3 $4) (look_at:<e,<e,t>> $2 $3)))))))
with :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (with:<e,<e,t>> $2 $3))))))
the :- NP\N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
bag :- N : (bag:<e,t>)
	   //SENTENCE 2
there :- NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
is :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
a :- NP\N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<e,t> $1))))
backpack :- N : (bag:<e,t>)
on :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3))))))
yellow :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (yellow:<e,t> $1))))
chair :- N : (chair:<e,t>)
	     // SENTENCE 3
both :- NP/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (two:<e,t> $1))))
guys :- N : (people:<e,t>)
set down :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (put_down:<e,<e,t>> $2 $3))))))
objects :- N : (objects:<e,t>)
	     //SENTENCE 4
walking :- S\NP : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (walk:<e,t> $1))))
away from :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (leave:<e,<e,t>> $2 $3))))))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<e,t> $1))))
// SENTENCE 9
walks :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $3 $4) ($1 $2) (move:<e,<e,t>> $2 $3)))))))
yellow :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (yellow:<e,t> $1) ($0 $1))))
to :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (goes_to:<e,<e,t>> $2 $3))))))
another :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
// SENTENCE 11
in :- NP/NP : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1))))
blue :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (blue:<e,t> $1) ($0 $1))))
jacket :- N : (shirt:<e,t>)
staring at :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (look_at:<e,t> $3))))))
// SENTENCE 12
guy :- N : (person:<e,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (in:<e,<e,t>> $2 $3))))))
plaid :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (plaid:<e,t> $1) ($0 $1))))
shirt :- N : (shirt:<e,t>)
picks up :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $2 $3) ($1 $4) (pick_up:<e,<e,t>> $3 $4)))))))
