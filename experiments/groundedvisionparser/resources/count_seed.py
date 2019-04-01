#!/usr/bin/env python

import re

def loadSeed(fname):
    with open(fname, 'r') as f:
        lines = map(lambda l: l.strip('\n').lstrip(), f.readlines())
        lines = filter(lambda l: not re.match('//.*', l), lines)
        lines = filter(lambda l: not l == '', lines)
        lines = sorted(list(set(lines)))
        return lines



if __name__ == '__main__':
    seed_lines = loadSeed('seed.lex')
    print seed_lines[0:20], len(seed_lines)
    
