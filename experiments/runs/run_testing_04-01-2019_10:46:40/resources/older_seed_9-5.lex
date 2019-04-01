// Determiners
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
a :- NP/NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
a :- N/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))

an :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
an :- NP/NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
an :- N/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))


of :- NP\NP/NP : (lambda $0:<e,t> (lambda $1:<<e,t>,<e,t>> (lambda $2:e (lambda $3:e ($0 $2)))))
is :- S\NP/PP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2))))))
is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $2 $4) ($1 $2 $3)))))))

// Conjunctional handling
and :- C : conj:c

// ADJ 
yellow :- NP/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (yellow:<e,t> $1) ($0 $1))))
green :- NP/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (green:<e,t> $1) ($0 $1))))
another :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))

// Connector 
with :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (hold:<e,<e,t>> $2 $3))))))

// Actions 
looking at :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> (look_at:<e,<e,t>> $1 $2) ($0 $2)))))

stares at :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> (look_at:<e,<e,t>> $2 $3) ($0 $3) ($1 $2))))))

lies :- (S\NP)/PP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2) (stationary:<e,t> $2))))))

sits :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2) (stationary:<e,t> $2))))))

// PP creators
in :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (in:<e,<e,t>> $1 $2)))))
on :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> (on:<e,<e,t>> $1 $2) ($0 $2)))))

// Extra 
middle :- NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))


// is :- S\N/PP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2))))))
// is :- S/PP : (lambda $1:<e,t> (lambda $2:e (and:<t*,t>  ($1 $2))))

// Actions
// picking up :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (pick_up:<e,<e,t>> $1 $2)))))
// next to :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (next_to:<e,<e,t>> $1 $2) (next_to:<e,<e,t>> $2 $1)))))
//from :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $1) (from:<e,<e,t>> $2 $1)))))
// wearing :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (wearing:<e,<e,t>> $1 $2)))))
// looking at :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (look_at:<e,<e,t>> $1 $2)))))


// dark-haired :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (dark-haired:<e,t> $1) ($0 $1))))
// blue :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (blue:<e,t> $1) ($0 $1))))
// yellow :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (yellow:<e,t> $1) ($0 $1))))
// red :- NP/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (red:<e,t> $1) ($0 $1))))
