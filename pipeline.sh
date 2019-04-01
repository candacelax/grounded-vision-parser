#!/bin/bash
set -euf -o pipefail

VISION_PARSER_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TIMESTAMP=$(date '+%m-%d-%Y_%H:%M:%S')

function usage {
    echo "Arguments:"
    # used for setting parameters for experiment
    echo "-d    data directory name"
    echo "-r    run name"
    echo "-e    number of epochs"
    echo "-g    size of beam of genlex"
    echo "-l    size of beam of parser"
    echo "-v    validation function; either `s` (stocgrad) `p` (perceptron) `sjc` (stocgrad joint with cluster)"
    echo "-t    type of supervision; either `f` (full) `s` (simulated weak) `w` (weak)" # TODO
    echo "-m    path for loading saved model"
    echo "-s    if testing seed sentences, give sentence number"
    # used for simulating vision system
    echo "-n    amount of noise, between 0-100"
    # used for benchmarks
    echo "-R    reduce number of examples in training set"
    # general
    echo "-w    run model without training, e.g. test data on seed lexion"
    echo "-o    port number for cache"
    echo "-O    number of ports for cache"
    echo "-Z    create ports"
    echo "-T    number of threads for multi-threaded CKY parser"
    echo "-L    maximum sentence length; sentences of length will be filtered out"
    echo "-h    Help"
}

# -----  get arguments from user----- #
while getopts ":hd:r:e:g:l:v:t:m:s:n:R:wo:O:T:L:Z" opt; do
    case $opt in
	h)
	    usage
	    exit ;;
	# ---- REQUIRED ---- #
	d)
	    DATASET=$OPTARG ;;
	r)
	    RUN_NAME=$OPTARG ;;
	e)
	    EPOCHS=$OPTARG ;;
	g)
	    GBEAM=$OPTARG ;;
	l)
	    LEXBEAM=$OPTARG ;;
	v)
	    if [ "$OPTARG" == "p" ]; then
		VALIDATION=perceptron
	    elif [ "$OPTARG" == "s" ]; then
		VALIDATION=stocgrad
	    elif [ "$OPTARG" == "sjc" ]; then
		VALIDATION=stocgradjointcluster
	    else
		echo "Error! Invalid validation function. Exiting.."
		exit -1
	    fi ;;
	t) # either full, simulated weak, or weak
	    SUPERVISION_TYPE=$OPTARG ;;
	
	m) # path to load previous model
	    MODEL=$OPTARG
	    if [ ! -f $MODEL ]; then # check if model files exists
	    	echo "Invalid filename for model provided. Exiting..."
	    	exit -1
	    fi ;;
	s)
	    SEED=$OPTARG ;;
	# simulation
	n) # amount of noise; only for simulation
	    NOISE=$OPTARG ;;
	R)
	    PERCENTAGE_TRAINING=$OPTARG ;;
	# general
	w)
	    NO_TRAINING=true ;;
	o)
	    PORT_NUMBER=$OPTARG ;;
	O)
	    NUM_PORTS=$OPTARG ;;
	Z)
	    CREATE_PORTS=true ;;
	T)
	    NUM_THREADS=$OPTARG ;;
	L)
	    MAX_SENT_LENGTH=$OPTARG ;;
	\?)
	    echo "Invalid option: -$OPTARG" >&2
	    usage
	    exit -1;;
        :  )
	    echo "Missing option argument for -$OPTARG" >&2;
	    exit -1;;
    esac
done

# set up sentence trackers
if [[ -v CREATE_PORTS ]]; then
    cd st-for-parser
    timestamp=$( date +%F_%T )
    current_port=$PORT_NUMBER
    while [ $current_port -lt $((PORT_NUMBER+NUM_PORTS)) ]; do
        stack exec st-for-parser -- --port $current_port > logs/log_$timestamp\_$current_port &
        current_port=$((current_port+1))
    done

    cd ..
fi

if [[ ! -v DATASET ]]; then
    if [[ $SUPERVISION_TYPE == 's' ]]; then
	DATASET="visionparser"
    elif [[ $SUPERVISION_TYPE == 'w' ]]; then
	DATASET="groundedvisionparser"
    else
	echo "Error! Cannot infer dataset; explicitly provide"
    fi
fi

# ----- create run directory ----- #
mkdir -p lib/spf/parser.ccg.rules.coordination.lambda/src # until we fix this in build file
RELATIVE_RUN_DIR=run_$RUN_NAME\_$TIMESTAMP
CURRENT_RUN_DIR=$VISION_PARSER_DIR/experiments/runs/$RELATIVE_RUN_DIR
printf "Currently working in directory:\n\t%s\n\n" $CURRENT_RUN_DIR
mkdir $CURRENT_RUN_DIR
export VISION_PARSER_DIR CURRENT_RUN_DIR NUM_FOLDS RELATIVE_RUN_DIR

# copy files for experiment into run directory
cp $VISION_PARSER_DIR/experiments/$DATASET/training_full.ccg \
   $VISION_PARSER_DIR/experiments/$DATASET/training_weak.ccg \
   $VISION_PARSER_DIR/experiments/$DATASET/test.ccg \
   $CURRENT_RUN_DIR
cp -r $VISION_PARSER_DIR/experiments/$DATASET/resources/ \
   $CURRENT_RUN_DIR


# use sentences that went into building seed lexicon
if [[ -v SEED ]]; then
    echo "Using seed files"
    cp $VISION_PARSER_DIR/experiments/$DATASET/vp-seed-files/sentence$SEED\training_full.ccg \
       $CURRENT_RUN_DIR/training_full.ccg
    cp $VISION_PARSER_DIR/experiments/$DATASET/vp-seed-files/sentence$SEED\training_weak.ccg \
       $CURRENT_RUN_DIR/training_weak.ccg
    cp $VISION_PARSER_DIR/experiments/$DATASET/vp-seed-files/sentence$SEED\test.ccg \
       $CURRENT_RUN_DIR/test.ccg
    cp $VISION_PARSER_DIR/experiments/$DATASET/vp-seed-files/sentence$SEED\seed.lex \
       $CURRENT_RUN_DIR/resources/seed.lex
    cp $VISION_PARSER_DIR/experiments/$DATASET/vp-seed-files/sentence$SEED\np-list.lex \
       $CURRENT_RUN_DIR/resources/np-list.lex
fi


#-------- run setup -----------#
HASH=$RUN_NAME\_$(md5sum <<< $RELATIVE_RUN_DIR | cut -f 1 -d ' ')
WORD_SKIPPING_COST=-1
SETUP="--vis $VISION_PARSER_DIR
	     --data $VISION_PARSER_DIR/experiments/$DATASET
	     --run $CURRENT_RUN_DIR
	     --name $RUN_NAME
	     --ep $EPOCHS
	     --val $VALIDATION
             --super $SUPERVISION_TYPE
	     --gbeam $GBEAM
	     --lexbeam $LEXBEAM
	     --hash $HASH
	     --skipcost =$WORD_SKIPPING_COST"


# TODO add option run once before training
# load model
if [[ -v MODEL ]]; then
    SETUP+=" --load $MODEL"
fi
# add noise
if [[ -v NOISE ]]; then
    SETUP+=" --noise $NOISE"
fi
# specify parser rules; uses all by default
# TODO incorporate rules
# RULES=true
# if [[ -v RULES ]]; then # TODO get rules from args
#     SETUP+=" --rules ruleComp,ruleApp,ruleCoord,shiftADJ,ruleRaiseComp"
# fi
# run without training
if [[ -v NO_TRAINING ]]; then
    SETUP+=" --seed_test"
fi
# port number for cache
if [[ -v PORT_NUMBER ]]; then
    SETUP+=" --port $PORT_NUMBER"
fi
if [[ -v NUM_PORTS ]]; then
    SETUP+=" --num_ports $NUM_PORTS"
fi
# number of threads for multi-threaded CKY parser
# port number for cache
if [[ -v NUM_THREADS ]]; then
    SETUP+=" --num_threads $NUM_THREADS"
fi
if [[ -v MAX_SENT_LENGTH ]]; then
    SETUP+=" --max_length $MAX_SENT_LENGTH"
fi

echo $SETUP
$VISION_PARSER_DIR/scripts-for-setup/set_up_experiment.py $SETUP

#------ reduce number of training examples for benchmarking ------ #
if [[ -v PERCENTAGE_TRAINING ]]; then
    if [[ -v SEED ]]; then
	echo 'Cannot reduce training size when using seed lexicon sentences! Skipping...'
    else
	echo 'Reducing size of training data..'
	$VISION_PARSER_DIR/reduce_sets.py \
	    -p $PERCENTAGE_TRAINING \
	    -e $VISION_PARSER_DIR/experiments/visionparser/ \
	    -r $CURRENT_RUN_DIR
    fi
fi

# ----- compile project in run directory ----- #
echo $RUN_NAME $HASH
$VISION_PARSER_DIR/compile.sh build.properties.$HASH
rm $VISION_PARSER_DIR/lib/spf/build.properties.$HASH
printf 'Finished compile\n\n'

# ----- run model ----- #
time $VISION_PARSER_DIR/run.sh \
     $VISION_PARSER_DIR/lib/spf/dist/spf-2.0.$HASH.jar \
     $CURRENT_RUN_DIR/$RUN_NAME.exp \
     $DATASET
exit

# ----- format stats from log files ----- #
$VISION_PARSER_DIR/format_experiment_results.py \
    -r $CURRENT_RUN_DIR \
    -e $EPOCHS
