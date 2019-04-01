import re

def writeOutputFile(filename, lines):
    with open(filename, 'w') as f:
        f.writelines('\n'.join(lines))

        
''' paths for training and test data '''
def writeDataFile(data_filename, use_weak_supervision):
    if use_weak_supervision:
        lines = [
            'type=data.grounded.sent id=traindata file=training_weak.ccg',
            'type=data.single id=testdata file=test.ccg'
        ]
    else:
        lines = [
            'type=data.single id=traindata file=training_full.ccg',
            'type=data.single id=testdata file=test.ccg'
        ]
    writeOutputFile(data_filename, lines)


def writeGenlexFile(genlex_filename, using_weak_supervision, using_partial_match, maxTokens=4):
    if using_weak_supervision:
        lines = [
            '## GENLEX method',
            'type=genlex.template.coarse id=genlex maxTokens=%i parser=parser beam=%%{lexbeam} ontology=ontology' % maxTokens
            ]
    elif using_partial_match:
        lines = [
            '## GENLEX method',
            'type=genlex.template.simulation id=genlex maxTokens=%i parser=parser beam=%%{lexbeam} ontology=ontology' % maxTokens
        ]
    else:
        lines = [
            '## GENLEX method',
            'type=genlex.template.supervised id=genlex maxTokens=%i beam=%%{lexbeam}' % maxTokens
            ]
    writeOutputFile(genlex_filename, lines)

def writeInitFile(init_filename):
    lines = [
        '## Model initializers',
        'type=init.lex   id=initSeedLex lexicon=seedLexicon',
        'type=init.lex	id=initNPs	lexicon=npLexicon',
        'type=scorer.lenexp id=expScorer coef=10.0 exp=1.1',
        'type=init.lex.weights id=initSeedLexWeights lexicon=seedLexicon key=FACLEX#LEX scorer=expScorer',
        'type=init.lex.weights id=initSeedXemeWeights lexicon=seedLexicon key=FACLEX#XEME value=10.0',
        'type=init.lex.weights id=initNPsLexWeights lexicon=npLexicon key=FACLEX#LEX scorer=expScorer',
        'type=init.lex.weights id=initNPsXemeWeights lexicon=npLexicon key=FACLEX#XEME value=10.0',
        'type=init.weights id=initWeights file=resources/init.weights']
    writeOutputFile(init_filename, lines)
    
    
''' jobs to run, which will nearly always be all of them '''
def writeJobsFile(job_filename, validation_function):    
    lines = [
        '## Jobs',
        '# Initialize',
        'type=init model=model init=initWeights,initSeedLex,initNPs,initSeedLexWeights,initNPsLexWeights,initSeedXemeWeights,initNPsXemeWeights id=init',
        '# Log initial model',
        'type=log id=log.pre model=model logger=loggerModel dep=init',
        '# Learning',
        'type=train learner=%s id=train dep=log.pre model=model' % validation_function,
        '# Log and test final model',
        'type=log id=log.post model=model logger=loggerModel dep=train',
        'type=test id=test tester=tester model=model dep=train',
        'type=save model=model file=%{outputDir}/model.sp id=save dep=train'
    ]
    writeOutputFile(job_filename, lines)


''' validation functions and parameters '''
def writeLearnFile(learn_filename, training_noise, run_before_training,
                   validation_function, using_weak_supervision,
                   using_partial_match, skip_to_test):
    lines = [
        '## Tester',
        'type=tester id=tester data=test parser=parser',
        '## Filter to skip long sentences during learning',
        'type=filter.sentence.length id=learningFilter length=%{trainingMaxSentenceLength}',
        '## Learner',
        # perceptron
        'type=learner.validation.perceptron id=perceptron data=train genlexbeam=%%{lexbeam} iter=%%{gIter} validator=validator hard=true filter=learningFilter genlex=genlex conflateParses=true errorDriven=true noise=%d runModelBeforeTraining=%s skipToTest=%s' % (training_noise, run_before_training, skip_to_test),
        # stocgrad
        'type=learner.validation.stocgrad id=stocgrad data=train genlexbeam=%%{lexbeam} iter=%%{gIter} validator=validator filter=learningFilter tester=tester genlex=genlex conflateParses=false errorDriven=true noise=%d runModelBeforeTraining=%s skipToTest=%s' % (training_noise, run_before_training, skip_to_test),
        # stocgrad joint with cluster
        'type=learner.validation.stocgradjointcluster id=stocgradjointcluster data=train genlexbeam=%%{lexbeam} iter=%%{gIter} validator=validator filter=learningFilter tester=tester genlex=genlex conflateParses=false errorDriven=true noise=%d runModelBeforeTraining=%s skipToTest=%s' % (training_noise, run_before_training, skip_to_test)
    ]

    if using_weak_supervision:
        lines = [
            '## Tester',
            'type=tester id=tester data=test parser=parser',
            '## Filter to skip long sentences during learning',
            'type=filter.sentence.length id=learningFilter length=%{trainingMaxSentenceLength}',
            '## Learner',
            # stocgrad
            'type=learner.validation.stocgrad.weak id=stocgrad data=train genlexbeam=%%{lexbeam} iter=%%{gIter} validator=validator filter=learningFilter tester=tester genlex=genlex conflateParses=false errorDriven=true runModelBeforeTraining=%s skipToTest=%s' % (run_before_training, skip_to_test)
        ]
    writeOutputFile(learn_filename, lines)


''' features for model; type of lexicon '''
def writeModelFile(model_filename, saved_model, run_without_training):
    # TODO get scale, type of lexicon, features as parameters
    lines = [
        '## Default scorers',
        'type=scorer.uniform id=uniform0Scorer   class=lexEntry  weight=0.0',
        'type=scorer.lex.skipping id=skippingScorer cost=%{wordSkippingCost} baseScorer=uniform0Scorer',
        
        '## Features',
        'type=feat.logexp.coordination id=featLogexpCoordination cpp1=true cpap=true rept=true',
        'type=feat.lex.factored id=featLex init=skippingScorer templateScale=0.1 entryScorer=skippingScorer',
        'type=feat.lex.dynskip id=featSkip',
        'type=feat.rules.count id=featRules scale=0.1',
        'type=feat.predicate id=featPredicate',
        'type=feat.duplicate id=featDuplicate',
        
        '## Lexicon',
        'type=lexicon.factored id=lexicon',
        
        '## Actual model',
        'type=model id=model lexicon=lexicon features=featLex,featLogexpCoordination,featRules,featSkip'+ (' seedBenchmark=true' if run_without_training else '') + (' file=' + saved_model if saved_model else ''),

        '## Default model logger',
        'type=logger.model id=loggerModel cluster=true'
    ]
    writeOutputFile(model_filename, lines)


def writeParamsFile(learn_filename, epochs, gbeam, lexbeam,
                    word_skipping_cost, max_sentence_length):
    lines = [
        '# Paths are relative to the .exp file using the parameters file',
        'ont=resources/parser.consts.ont:resources/parser.preds.ont',
        'types=resources/generic.types',
        'seedlex=resources/seed.lex',
        'nplist=resources/np-list.lex',
        'gBeam=%i' % gbeam,
        'lexbeam=%i' % lexbeam,
        'gIter=%i' % epochs,
        'wordSkippingCost=%s' % word_skipping_cost,
        'trainingMaxSentenceLength=%s' % max_sentence_length
    ]
    writeOutputFile(learn_filename, lines)


        
''' rules for parser; parameters for beam
''' 
def writeParserFile(parser_filename, using_weak_supervision,
                    num_threads, which_rules):
    # rules=ruleComp,ruleApp,ruleCoord,shiftPP
    if which_rules == None:
        # use default rules
        rules = 'ruleComp,ruleApp,ruleRaiseComp'
    else:
        rules = which_rules

        # note: cannot use prechart pruning with gradient-based learning!
    if using_weak_supervision:
        lines = ['# Parsing rules',
                 'type=rule.composition id=ruleComp',
                 'type=rule.application id=ruleApp',
                 #'type=rule.shifting.adj id=shiftADJ',
                 'type=rule.typeraise.composition.forward id=ruleRaiseComp',
                 #'type=rule.thatless id=ruleThatless',
                 #'type=rule.shift.pluralexists id=rulePluralExists',
                 #'type=rule.coordination id=ruleCoord',
                 #'type=rule.coordination.simple id=ruleCoordSimple',
                 '# NF constraints',
                 'type=nf.eisner id=nfValidator',
                 '# Parse filter',
                 'type=parsefilter.lambda.simple id=parseFilter syntax=S',
                 '# CKY Parser',
                 # note: cannot use prechart pruning with gradient-based learning!
                 'type=parser.cky.multi id=parser parseFilter=parseFilter beam=%{gBeam} rules='+rules +' executor=executor nfValidator=nfValidator wordSkipping=simple pruneLexicalCells=true' + (' threads=' + num_threads  if num_threads else '')]   
    else:
        lines = ['# Parsing rules',
                 'type=rule.composition id=ruleComp',
                 'type=rule.application id=ruleApp',
                 'type=rule.typeraise.composition.forward id=ruleRaiseComp',
                 'type=rule.thatless id=ruleThatless',
                 'type=rule.shift.pluralexists id=rulePluralExists',
                 'type=rule.coordination id=ruleCoord',
                 'type=rule.coordination.simple id=ruleCoordSimple',
                 '# NF constraints',
                 'type=nf.eisner id=nfValidator',
                 '# Parse filter',
                 'type=parsefilter.lambda.simple id=parseFilter syntax=S',
                 '# CKY Parser',
                 'type=parser.cky.multi id=parser parseFilter=parseFilter beam=%{gBeam} preChartPruning=true rules='+rules +' executor=executor nfValidator=nfValidator wordSkipping=simple'+ (' threads=' + num_threads  if num_threads else ''),
                 'type=parser.filter.supervised id=filterFactory']
    writeOutputFile(parser_filename, lines)

    
''' defined type of validator; either weak or labeled
''' 
def writeValidationFile(validation_filename, using_weak_supervision, using_partial_match, port_for_cache, num_ports):
    if using_weak_supervision:
        ports = '_'.join(map(lambda i: str(int(port_for_cache)+i),
                             range(0, int(num_ports))))
        lines =[
            '## Validation functions',
            'type=validator.sentence.tracker id=validator ports=%s' % ports
        ]
    elif using_partial_match:
        port_for_cache = str(port_for_cache)
        lines =[
            '## Validation functions',
            'type=validator.labeled.permute id=validator' + (' cache='+ port_for_cache if port_for_cache else '')
            ]
                
    else:
        lines =[
            '## Validation functions',
            'type=validator.labeled id=validator'
        ]            

    writeOutputFile(validation_filename, lines)
