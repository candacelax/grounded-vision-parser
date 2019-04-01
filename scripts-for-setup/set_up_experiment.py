#!/usr/bin/env python

'''Configure the MongoDB database with entities, types, and sentence data.

Usage:
set_up_experiment.py (--vis <vision_directory>) (--data <data_directory>) (--run <run_directory>)
                     (--name <run_name>) (--ep <epochs>) (--val <validation_function>)
                     (--super <supervision_type>) (--gbeam <gbeam>) (--lexbeam <lexbeam>)
                     (--hash <hash>) (--skipcost <word_skipping_cost>)
                     [options]

Options:
    --load=<load_model>                        path to model to load for this run
    --noise=<noise>                            amount of noise to add to training [default: 0.0] 
    --seed_training                            run model on training data once before training
    --seed_test                                run model on test data without any training
    --rules=<rules>                            rules for model
    --partial                                  partial match training; simulates vision system
    --port=<port>                              port for cache
    --num_ports=<num_ports>                    num/ports for thread
    --weak                                     using weak supervision
    --num_threads=<num_threads>                num/threads for multi-threaded CKY parser
    --max_length=<max_length>                  max length for training sentences [default: 50]
'''

from docopt import docopt
from os import path
import re

from write_inc_files import *
from write_build_properties_file import *



''' makes sure arguments are valid; converts types '''
def formatArgs(args):
    formatted_args = {}
    formatted_args['<vision_directory>'] = args['<vision_directory>']
    formatted_args['<data_directory>'] = args['<data_directory>']
    formatted_args['<run_directory>'] = args['<run_directory>']
    formatted_args['<run_name>'] = args['<run_name>']
    formatted_args['<hash>'] = args['<hash>']
    formatted_args['<load_model>'] = path.abspath(args['--load']) if args['--load'] else args['--load']
    formatted_args['<rules>'] = args['--rules']
    formatted_args['<num_threads>'] = args['--num_threads']
    formatted_args['<num_ports>'] = args['--num_ports'] if args['--num_ports'] else 1

    # check for valid validation function
    if not re.match('stocgrad|perceptron', args['<validation_function>']):
        raise ValueError('Invalid option for validation function argument!')
    formatted_args['<validation_function>'] = args['<validation_function>']
    
    # convert integers
    formatted_args['<epochs>'] = int(args['<epochs>'])
    formatted_args['<gbeam>'] = int(args['<gbeam>'])
    formatted_args['<lexbeam>'] = int(args['<lexbeam>'])
    formatted_args['<max_sentence_length>'] = int(args['--max_length']) if args['--max_length'] else 50
    formatted_args['<port_for_cache>'] = int(args['--port']) if args['--port'] else None
        

        
    # convert floats
    formatted_args['<add_noise>'] = float(args['--noise'])
    formatted_args['<word_skipping_cost>'] = float(args['<word_skipping_cost>'][1:]) # TODO clean up
    
    # check inputs are valid
    assert formatted_args['<epochs>'] > 0
    assert formatted_args['<gbeam>'] > 0
    assert formatted_args['<lexbeam>'] > 0
    assert formatted_args['<add_noise>'] >= 0 and formatted_args['<add_noise>'] <= 100

    # convert booleans
    formatted_args['<run_training_data_on_seed>'] = True if args['--seed_training'] else False
    formatted_args['<using_partial_match>'] = True if re.match('s', args['<supervision_type>']) else False
    formatted_args['<using_weak_supervision>'] = True if re.match('w', args['<supervision_type>']) else False
    formatted_args['<run_test_data_on_seed>'] = True if args['--seed_test'] else False
    return formatted_args
        

def writeGlobalParams(filename):
    with open(filename, 'a') as f:
        f.write('include=params.inc\n')
        f.write('outputDir=logs\n')
        f.write('globalLog=logs/global.log\n\n')

def writeResources(run_filename):
    with open(run_filename, 'a') as f:
        # parser
        f.write('#Parser\ninclude=parser.inc\n')

        # model
        f.write('#Model\ninclude=model.inc\n')

        # validation function
        f.write('#Validation function\ninclude=validation.inc\n')
        
        # lexical generator
        f.write('#Lexical Generator\ninclude=genlex.inc\n')

        # training and test sets
        f.write('#Training and test sets\n')
        f.write('include=data.inc\n')
        f.write('type=data.composite id=train sets=traindata\n')
        f.write('type=data.composite id=test sets=testdata\n')
        
        # learner and tester
        f.write('#Learner and tester\ninclude=learn.inc\n')
        
        # initializer
        f.write('#Initializers\ninclude=init.inc\n\n')

        
def writeJobs(filename):
    with open(filename, 'a') as f:
        f.write('# Jobs\ninclude=jobs.inc\n')
        

def createExperimentFile(run_directory, run_name):
    writeGlobalParams(path.join(run_directory, '{}.exp'.format(run_name)))
    writeResources(path.join(run_directory, '{}.exp'.format(run_name)))
    writeJobs(path.join(run_directory, '{}.exp'.format(run_name)))
            
if __name__ == '__main__':
    args = formatArgs(docopt(__doc__))
    createExperimentFile(args['<run_directory>'],
                         args['<run_name>'])

    
    # we use this to provide name of jar file
    write_build_properties_file(path.join(args['<vision_directory>'],
                                          'lib',
                                          'spf',
                                          'build.properties.{}'.format(args['<hash>'])),
                                args['<hash>'])

    # defines locations of training, test data
    writeDataFile(path.join(args['<run_directory>'], 'data.inc'),
                  args['<using_weak_supervision>'])

    # defines creators for supervised and coarse genlex
    writeGenlexFile(path.join(args['<run_directory>'], 'genlex.inc'),
                    args['<using_weak_supervision>'],
                    args['<using_partial_match>'])
    
    # order to run jobs, e.g. model init, training, testing
    writeJobsFile(path.join(args['<run_directory>'], 'jobs.inc'),
                  args['<validation_function>'])

    writeModelFile(path.join(args['<run_directory>'], 'model.inc'),
                   args['<load_model>'],
                   args['<run_test_data_on_seed>'])

    # defines validation function
    writeLearnFile(path.join(args['<run_directory>'], 'learn.inc'),
                   args['<add_noise>'],
                   args['<run_training_data_on_seed>'],
                   args['<validation_function>'],
                   args['<using_weak_supervision>'],
                   args['<using_partial_match>'],
                   args['<run_test_data_on_seed>'])

    # defines parameters of our model
    writeParamsFile(path.join(args['<run_directory>'], 'params.inc'),
                    args['<epochs>'],
                    args['<gbeam>'],
                    args['<lexbeam>'],
                    args['<word_skipping_cost>'],
                    args['<max_sentence_length>'])

    writeValidationFile(path.join(args['<run_directory>'], 'validation.inc'),
                        args['<using_weak_supervision>'],
                        args['<using_partial_match>'],
                        args['<port_for_cache>'],
                        args['<num_ports>'])
    
    writeInitFile(path.join(args['<run_directory>'], 'init.inc'))
    
    # defines rules for parser
    writeParserFile(path.join(args['<run_directory>'], 'parser.inc'),
                    args['<using_weak_supervision>'],
                    args['<num_threads>'],
                    args['<rules>'])
