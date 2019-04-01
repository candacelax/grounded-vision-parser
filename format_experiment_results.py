#!/usr/bin/env python

import argparse, re
import numpy as np
from os import path
import matplotlib.pyplot as plt

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-run', dest='run_directory', type=str, required=True)
    parser.add_argument('-epochs',type=int, required=True)
    parser.add_argument('-eqtrain', dest='equality_metric_training',
                        type=str, required=True, action=BoolArg)
    parser.add_argument('-eqtest', dest='equality_metric_test',
                        type=str, required=True, action=BoolArg)
    args = parser.parse_args()
    return args

class BoolArg(argparse.Action):
    def __init__(self, option_strings, dest, nargs=None, **kwargs):
        if nargs is not None:
            raise ValueError('nargs not allowed')
        super(BoolArg, self).__init__(option_strings, dest, **kwargs)
    def __call__(self, parser, namespace, values, option_string=None):
        if re.match('true', values, re.IGNORECASE) or re.match('t', values, re.IGNORECASE):
            setattr(namespace, self.dest, True)
        elif re.match('false', values, re.IGNORECASE) or re.match('f', values, re.IGNORECASE):
            setattr(namespace, self.dest, False)
        else:
            raise ValueError('Invalid option for argument', \
                             option_string)

def getSizeSeedLexicon(filename):
    with open(filename, 'r') as f:
        num_entries = 0
        for line in f:
            if not re.match('.*//.*', line) and not re.match('\n', line):
                num_entries += 1
    return num_entries

def load_cost_trends(filename):
    with open(filename, 'r') as f:
        set_size = int(f.readline().strip('\n').split(' ')[-1])
        avg_cost_over_avg_length = float(f.readline().strip('\n').split(' ')[-1])
        size_annotated_sentences = int(f.readline().strip('\n').split(' ')[-1])
        
        costs_by_num_predicates = np.empty((size_annotated_sentences, 2))
        counter = 0
        f.readline() # reading `sum_over_length` line
        for line in f:
            if re.match('\(.*\)', line):
                continue
            else:
                stats = map(lambda i: float(i), line.strip('\n').split(' '))
                cost_over_length = stats[0]
                cost = stats[1]
                length = stats[2] # i.e. num/predicates

                # TODO populate both cells same time
                costs_by_num_predicates[counter, 0] = cost
                costs_by_num_predicates[counter, 1] = length
                counter+=1
    return set_size, avg_cost_over_avg_length, costs_by_num_predicates


def write_cummulative_stats(cummulative_stats_filename, run_directory,
                            training_precision, training_recall, training_f1,
                            test_precision, test_recall, test_f1,
                            num_training_examples,
                            num_annotated_sentences,
                            training_avg_cost_over_avg_num_predicates,
                            test_avg_cost_over_avg_num_predicates,
                            training_costs_by_num_predicates,
                            test_costs_by_num_predicates,
                            num_epochs,
                            num_entries_seed_lexicon):
    with open(cummulative_stats_filename, 'a') as f:
        f.write('{},{},{},{},{},{},{},{},{},{},{},{},{}\n'.format(
                (run_directory,
                 training_precision,
                 training_recall,
                 training_f1,
                 test_precision,
                 test_recall,
                 test_f1,
                 num_training_examples,                 
                 num_annotated_sentences,
                 training_avg_cost_over_avg_num_predicates,
                 training_avg_cost_over_avg_num_predicates,
                 num_epochs,
                 num_entries_seed_lexicon)))

def plot_results(filename, costs_by_length, which_set):
    plt.hist(costs_by_length[:, 1], 2)
    plt.xlabel('num_predicates')
    plt.ylabel('cost')
    plt.title('{} {}'.format(which_set, filename))
    plt.savefig(filename)
    plt.show()


def get_stats(lines):
    for l in lines:
        if re.match('.*precision:.*', l, re.IGNORECASE):
            precision = float(l.strip('\n').split(' ')[-1])
        elif re.match('.*recall:.*', l, re.IGNORECASE):
            recall = float(l.strip('\n').split(' ')[-1])
        elif re.match('.*f1:.*', l, re.IGNORECASE):
            f1 = float(l.strip('\n').split(' ')[-1])
    return precision, recall, f1
    
if __name__ == '__main__':
    args = parse_args()

    num_entries_seed_lexicon = getSizeSeedLexicon(path.join(args.run_directory,
                                                            'resources',
                                                            'seed.lex'))


    # get training and test stats
    with open(path.join(args.run_directory, 'logs', 'final_accuracy_train.txt'), 'r') as f_train,\
         open(path.join(args.run_directory, 'logs', 'final_accuracy_test.txt'), 'r') as f_test:
        training_precision, training_recall, training_f1 = get_stats(f_train.readlines())
        test_precision, test_recall, test_f1 = get_stats(f_test.readlines())

    
    # get costs per num of predicates for training and test sets
    training_set_size, training_avg_cost_over_avg_num_predicates, training_costs_by_num_predicates =\
                load_cost_trends(path.join(args.run_directory,
                                           'logs',
                                           'trends_train.txt'))
    test_set_size, test_avg_cost_over_avg_num_predicates, test_costs_by_num_predicates =\
                load_cost_trends(path.join(args.run_directory,
                                           'logs',
                                           'trends_test.txt'))
        
    # print & save stats
    print 'Training (using {} equality metric)'.format
        ('our' if args.equality_metric_training else 'their')
    print 'Test (using {} equality metric)'.format(
        ('our' if args.equality_metric_test else 'their'))
    print 'Number of annotated training sentences: {}'.format(len(training_costs_by_num_predicates))
    print 'Number of test sentences: {}'.format(len(test_costs_by_num_predicates))
    print '\ttraining precision: {}\n\ttraining recall: {}\n\ttraining F1: {}\n\n\n'.format(
        (training_precision, training_recall, training_f1))
    print '\ttest precision: {}\n\ttest recall: {}\n\ttest F1: {}\n\n\n'.format(
        (test_precision, test_recall, test_f1))
    
    with open('{}/statistics.txt'.format(args.run_directory), 'a') as f:
        f.write('Training (using {} equality metric)\n'.format(
                ('our' if args.equality_metric_training else 'their')))
        f.write('Test (using {} equality metric)\n'.format(
                ('our' if args.equality_metric_test else 'their')))
        f.write('\nNumber of annotated training sentences: {}\n'.format(len(training_costs_by_num_predicates)))
        f.write('\nNumber of test sentences: {}\n'.format(len(test_costs_by_num_predicates)))
        f.write('\ttraining precision: {}\n\ttraining recall: {}\n\ttraining F1: {}\n\n\n'.format(
                (training_precision, training_recall, training_f1)))
        f.write('\ttest precision: {}\n\ttest recall: {}\n\ttest F1: {}\n\n\n'.format(
                (test_precision, test_recall, test_f1)))

        

        
        
    # write cummalative stats for across-experiment comparison
    cummulative_stats_filename = 'cummulative_stats_train={}_test={}.csv'.format(
                                 (str(args.equality_metric_training).lower(),
                                  str(args.equality_metric_test).lower()))
    write_cummulative_stats(cummulative_stats_filename,
                            args.run_directory,
                            training_precision,
                            training_recall,
                            training_f1,
                            test_precision,
                            test_recall,
                            test_f1,
                            training_set_size,
                            len(training_costs_by_num_predicates),
                            training_avg_cost_over_avg_num_predicates,
                            test_avg_cost_over_avg_num_predicates,
                            training_costs_by_num_predicates,
                            test_costs_by_num_predicates,
                            args.epochs,
                            num_entries_seed_lexicon)
        
    # plot stats        
    #plot_results(path.join(args.run_directory, 'trends_training_fig.png'),
    #             training_costs_by_num_predicates,
    #             'training')
    # plot_results(path.join(args.run_directory, 'trends_test_fig.png'),
    #              costs,
    #              'test')


    
