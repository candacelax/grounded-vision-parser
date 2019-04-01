#!/usr/bin/env python

'''
 Reduce amount of training data being used in run; for benchmarks

Usage:
 reduce_sets.py (-p <percentage_of_train_data>) (-e <exp_directory>) (-r <current_run_directory>)
'''

from docopt import docopt
from os import path
from math import floor
from random import shuffle
import re

def formatArgs(args):
    args['<percentage_of_train_data>'] = float(args['<percentage_of_train_data>'])
    return args

def loadAndReduceSentences(sentences_only_filename, percentage_to_keep):
    # load all sentences
    with open(sentences_only_filename, 'r') as f:
        sentences = f.readlines()
        shuffle(sentences)
        
    # reduce number of sentences
    num_examples_to_keep = int( floor(len(sentences) * (percentage_to_keep/100)) )
    sentences = sentences[0 : num_examples_to_keep]
    return sentences

def loadTrainingData(sentences, examples_filename, full_supervision_filename, weak_supervision_filename):
    with open(examples_filename, 'r') as f:
        lines = f.readlines()

    counter=0
    with open(full_supervision_filename, 'w') as full, \
         open(weak_supervision_filename, 'w') as weak:
        for i in range(0, len(lines)):
            if re.match('\n', lines[i]) or re.match('//.*', lines[i]):
                full.write(lines[i])
                weak.write(lines[i])

            else:
                if lines[i] in sentences:
                    counter+=1
                    sentence = lines[i].strip('\n')
                    video_id = lines[i+1].strip('\n')
                    logical_form = lines[i+2].strip('\n')
                    
                    full.write('{}\n{}\n{}\n'.format(sentence, video_id, logical_form))
                    weak.write('{}\n{}\n'.format(sentence, video_id))
                    
    print 'kept {} examples for training'.format(counter)

if __name__ == '__main__':
    args = formatArgs(docopt(__doc__))
    
    sentences = loadAndReduceSentences(path.join(args['<exp_directory>'],
                                                 'sentences_only_training.txt'),
                                       args['<percentage_of_train_data>'],)

    loadTrainingData(sentences,
                     path.join(args['<exp_directory>'],
                               'training_full.ccg'),
                     path.join(args['<current_run_directory>'],
                               'training_full.ccg'),
                     path.join(args['<current_run_directory>'],
                               'training_weak.ccg'))
    
