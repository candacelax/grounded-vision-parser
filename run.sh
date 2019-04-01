#!/bin/bash

DIST_FILE=$1
EXPERIMENT_FILE=$2
DATASET=$3

if [[ -n $EXPERIMENT_FILE ]]; then
    java -jar $DIST_FILE $EXPERIMENT_FILE $DATASET
else
    echo "Experiment file not found!"
fi
