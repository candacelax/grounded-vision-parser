# Parsing rules
type=rule.composition id=ruleComp
type=rule.application id=ruleApp
type=rule.typeraise.composition.forward id=ruleRaiseComp
type=rule.thatless id=ruleThatless
type=rule.shift.pluralexists id=rulePluralExists
type=rule.coordination id=ruleCoord
type=rule.coordination.simple id=ruleCoordSimple
# NF constraints
type=nf.eisner id=nfValidator
# Parse filter
type=parsefilter.lambda.simple id=parseFilter syntax=S
# CKY Parser
type=parser.cky.multi id=parser parseFilter=parseFilter beam=%{gBeam} preChartPruning=true rules=ruleComp,ruleApp,ruleRaiseComp executor=executor nfValidator=nfValidator wordSkipping=simple
type=parser.filter.supervised id=filterFactory