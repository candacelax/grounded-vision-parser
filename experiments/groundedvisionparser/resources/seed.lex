//--- SENTENCE 1 --- FIRST PARSE
// she put the orange car on the desk
she :- NP : (lambda $0:e (person:<p,t> $0))
put :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<p,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2) ($0 $3 $4) (put_down:<a,<o,t>> $2 $3)))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
orange :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (orange:<o,t> $1) ($0 $1))))
car :- N : (car:<o,t>)
on :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3))))))
desk :- N : (table:<o,t>)
//--- SENTENCE 2 --- FIRST PARSE
// there is a green backpack sitting on a yellow chair
there is :- S/NP : (lambda $0:<e,<e,t>> (lambda $1:e (lambda $2:e ($0 $1 $2))))
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
green :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<o,t> $1))))
backpack :- N : (bag:<o,t>)
//sitting :- NP\NP : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (sit:<e,t> $1))))
on :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3))))))
yellow :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> ($0 $1) (yellow:<o,t> $1))))
chair :- N : (chair:<o,t>)
//--- SENTENCE 3 --- FIRST PARSE
// both guys set down the objects at the same time
both :- NP/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (two:<e,t> $1))))
guys :- N : (person:<p,t>)
set down :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (put_down:<a,<e,t>> $2 $3))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
//objects :- N : (objects:<o,t>)
//at the same time :- NP\NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
//--- SENTENCE 4 --- FIRST PARSE
// a man is walking away from a green chair
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
man :- N : (person:<p,t>)
is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2))))))
//walking away from :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (walk:<p,t> $1) (from:<e,<e,t>> $1 $2)))))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<o,t> $1))))
chair :- N : (chair:<o,t>)
//--- SENTENCE 5 --- FIRST PARSE
// a green backpack is near the stairway on the floor
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
green :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (green:<o,t> $1) ($0 $1))))
backpack :- N : (bag:<o,t>)
is :- ((S\NP)/NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:<e,t> (lambda $3:e (lambda $4:e (lambda $5:e (and:<t*,t> ($0 $3 $4) ($2 $3) ($1 $3 $5))))))))
near :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (near:<e,<e,t>> $1 $2)))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
//stairway :- N : (stairs:<e,t>)
on :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (on:<e,<e,t>> $1 $2))))
floor :- N : (floor:<e,t>)
//--- SENTENCE 6 --- FIRST PARSE
// a woman cautiously grabs an apple off the table
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
woman :- N : (person:<p,t>)
	       //cautiously :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($1 $2) ($0 $2 $3))))))
grabs :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (pick_up:<a,<o,t>> $1 $2)))))
an :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
apple :- N : (apple:<o,t>)
off :- (S\S)/NP : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2 $3) ($0 $4) (from:<e,<e,t>> $3 $4)))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
table :- N : (table:<o,t>)
//--- SENTENCE 7 --- FIRST PARSE
// the guy in the plaid shirt walks towards the tan chair
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
guy :- N : (person:<p,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($1 $2) ($0 $3) (in:<e,<o,t>> $2 $3))))))
plaid :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (plaid:<o,t> $1) ($0 $1))))
shirt :- N : (shirt:<o,t>)
walks :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2 $3) ($0 $2 $4) (walk:<p,t> $2)))))))
stands :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2 $3) ($0 $2 $4) (stand:<p,t> $2)))))))
towards :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (toward:<e,<e,t>> $1 $2)))))
tan :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (tan:<o,t> $1) ($0 $1))))
chair :- N : (chair:<o,t>)
//--- SENTENCE 8 --- FIRST PARSE
// a woman is standing by a table
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
woman :- N : (person:<p,t>)
is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2))))))
standing :- S\NP : (lambda $0:e (stand:<p,t> $0))
by :- ((S\NP)\(S\NP))/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (near:<e,<e,t>> $2 $3))))))
//by :- ((S\NP)\(S\NP))/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3))))))
table :- N : (table:<o,t>)
//--- SENTENCE 9 --- FIRST PARSE
// the man lifting the chair is wearing a plaid shirt
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
lifting :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<p,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (pick_up:<a,<o,t>> $2 $3))))))
chair :- N : (chair:<o,t>)
is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2 $3) ($0 $2 $4)))))))
wearing :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (wear:<a,<o,t>> $1 $2)))))
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
plaid :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (plaid:<o,t> $1) ($0 $1))))
shirt :- N : (shirt:<o,t>)
//--- SENTENCE 10 --- FIRST PARSE
// a man walks a yellow chair over to another man
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
walks :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2) ($0 $3 $4) (walk:<p,t> $2)))))))
yellow :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (yellow:<o,t> $1) ($0 $1))))
chair :- N : (chair:<o,t>)
over to :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (to:<e,<e,t>> $2 $3))))))
another :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
//--- SENTENCE 11 --- FIRST PARSE
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (in:<e,<e,t>> $2 $3))))))
blue :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (blue:<o,t> $1) ($0 $1))))
jacket :- N : (shirt:<e,t>)
is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2 $3) ($0 $2 $4))))))
staring at :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (look_at:<a,<e,t>> $1 $2)))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<o,t> $1))))
office chair :- N : (chair:<e,t>)
//--- SENTENCE 12 --- FIRST PARSE
// the guy in the plaid shirt picks up the green backpack
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
guy :- N : (person:<p,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (in:<e,<o,t>> $2 $3))))))
plaid :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (plaid:<o,t> $1) ($0 $1))))
shirt :- N : (shirt:<o,t>)
picks up :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2 $3) ($0 $4) (pick_up:<a,<o,t>> $2 $4)))))))
green :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<o,t> $1))))
backpack :- N : (bag:<o,t>)
//--- SENTENCE 13 --- FIRST PARSE
// the chair is green
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
chair :- N : (chair:<o,t>)
is :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (and:<t*,t> ($0 $2) ($1 $2)))))
green :- NP : (green:<o,t>)
//--- SENTENCE 14 --- FIRST PARSE
the:- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (in:<e,<e,t>> $2 $3))))))
red :- NP : (lambda $0:e (red:<o,t> $0))
is :- (S\NP)/(S\NP) : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $2 $3))))))
standing :- S\NP : (lambda $0:e (stand:<p,t> $0))
		       //very :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
		       //still :- (S\NP)\(S\NP) : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
//--- SENTENCE 15 --- FIRST PARSE
there is :- (S/NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $2 $3) ($1 $2 $4)))))))
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
in the middle of :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (in:<e,<o,t>> $2 $3))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
lobby :- N : (lobby:<e,t>)
picking up :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (pick_up:<a,<o,t>> $1 $2)))))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<o,t> $1))))
backpack :- N : (bag:<e,t>)
//--- SENTENCE 16 --- FIRST PARSE
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
man :- N : (person:<p,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($1 $2) ($0 $3) (in:<e,<e,t>> $2 $3))))))
plaid :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (plaid:<o,t> $1) ($0 $1))))
shirt :- N : (shirt:<o,t>)
is :- ((S\NP)/NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:<e,<e,t>> (lambda $3:e (lambda $4:e (lambda $5:e (lambda $6:e (and:<t*,t> ($2 $3 $4) ($0 $3 $5) ($1 $5 $6)))))))))
sliding :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (move:<a,<o,t>> $1 $2)))))
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
green :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> ($0 $1) (green:<o,t> $1))))
chair :- N : (chair:<o,t>)
to :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $1) (to:<e,<e,t>> $3 $1))))))
his :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
right :- N : (right:<e,t>)
//--- SENTENCE 17 --- FIRST PARSE
			 // TODO the S\NP type needs to be avoided
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
apple :- N : (apple:<o,t>)
is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2))))))
placed :- S\NP : (lambda $0:e (lambda $1:e (put_down:<e,t> $0)))
on :- ((S\NP)\(S\NP))/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3)))))))
table :- N : (table:<o,t>)
//--- SENTENCE 19 --- FIRST PARSE
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
woman :- N : (person:<p,t>)
gives :- ((S\NP)/NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:<e,t> (lambda $3:e (lambda $4:e (lambda $5:e (and:<t*,t> ($0 $4) ($1 $5) ($2 $3) (give:<a,<e,<e,t>>> $3 $4 $5))))))))
man :- N : (person:<p,t>)
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
pear :- N : (pear:<o,t>)
//--- SENTENCE 20 --- FIRST PARSE
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
in :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (in:<e,<e,t>> $2 $3))))))
grey :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (gray:<o,t> $1) ($0 $1))))
shirt :- N : (shirt:<o,t>)
walks :- ((S\NP)/NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,<e,t>> (lambda $2:<e,<e,t>> (lambda $3:e (lambda $4:e (lambda $5:e (lambda $6:e (and:<t*,t> ($0 $3 $5) ($1 $5 $6) ($2 $3 $4) (walk:<p,t> $3)))))))))
away from :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (from:<e,<e,t>> $1 $2)))))
holding :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $1) (hold:<a,<o,t>> $3 $1))))))
yellow :- N/N : (lambda $0:<o,t> (lambda $1:e (and:<t*,t> (yellow:<o,t> $1) ($0 $1))))
backpack :- N : (bag:<o,t>)

// --- SENTENCE 21
she :- NP : (lambda $0:e (person:<p,t> $0))
moves :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2) ($0 $3 $4) (move:<a,<o,t>> $2 $3)))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
book :- N : (book:<o,t>)
across :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (across:<e,<e,t>> $2 $3))))))
table :- NP : (table:<o,t>)
	       
// --- SENTENCE 22
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
man :- N : (person:<p,t>)
sets :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2) ($0 $3 $4) (put_down:<a,<o,t>> $2 $3)))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
scope :- N : (telescope:<o,t>)
down on :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3))))))
green :- N/N : (lambda $0:<e,t> (lambda $1:o (and:<t*,t> (green:<o,t> $1) ($0 $1))))
backpack :- NP : (bag:<o,t>)

// --- SENTENCE 23
//walks towards :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2) ($1 $3) (walk:<p,t> $2) (toward:<e,<e,t>> $2 $3))))))

one :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
walks :- (S\NP)/NP: (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2) (walk:<p,t> $2))))))
towards :- PP/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (toward:<a,<e,t>> $1 $2)))))
another :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))


	       
// --- SENTENCE 24
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
man :- N : (person:<p,t>)
places :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<p,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2) ($0 $3 $4) (put_down:<a,<o,t>> $2 $3)))))))
an :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
	       //object :- N : (object:<o,t>)
on :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3))))))
the :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (green:<o,t> $1) ($0 $1))))
backpack :- N : (bag:<o,t>)


// SENTENCE 25
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
man :- N : (person:<p,t>)
holds out :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (hold:<a,<o,t>> $2 $3))))))
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
green :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> (green:<o,t> $1) ($0 $1))))
backpack :- N : (bag:<o,t>)

// SENTENCE 26
a :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1)))
man :- N : (person:<p,t>)
is :- (S\NP)/(S\NP) : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $2 $3) ($1 $2))))))
swinging :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $2) (move:<a,<o,t>> $1 $2)))))
his :- NP/N : (lambda $0:<e,t> (lambda $1:e ($0 $1))) 
backpack :- N : (bag:<o,t>)

  
2 :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (two:<e,t> $1))))
two :- N/N : (lambda $0:<e,t> (lambda $1:e (and:<t*,t> ($0 $1) (two:<e,t> $1))))
  //piece of fruit :- N : (fruit:<e,t>)

		      //there are :- S/NP : (lambda $0:<e,<e,t>> (lambda $1:e (lambda $2:e ($0 $1 $2))))
		      //there are :- S/NP : (lambda $0:<e,t> (lambda $1:e ($0 $1)))

  //place :- (S\NP)/NP : (lambda $0:<e,<e,t>> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($0 $2 $3) ($1 $4) (put_down:<e,<e,t>> $2 $4)))))))
  //in the air :- S\S : (lambda $0:<e,<e,t>> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $1 $2)))))
  
	       //next to :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ( $0 $2) ($1 $3) (near:<e,<e,t>> $3 $2))))))
		      //in front of :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (in_front_of:<e,<e,t>> $2 $3))))))
	       //on top of :- (NP\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,t> (lambda $2:e (lambda $3:e (and:<t*,t> ($0 $3) ($1 $2) (on:<e,<e,t>> $2 $3))))))

  
		      //reaches for :- (S\NP)/NP : (lambda $0:<e,t> (lambda $1:<e,<e,t>> (lambda $2:e (lambda $3:e (lambda $4:e (and:<t*,t> ($1 $2 $3) ($0 $4) (pick_up:<p,<o,t>> $2 $4)))))))
