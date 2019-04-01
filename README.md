# Grounded semantic parser

This is the code for our paper [Grounding language acquisition by training semantic parsers using captioned videos](https://www.aclweb.org/anthology/D18-1285). Please cite us if you use our code or data and feel free to reach out with any questions.


## Running the Project
### Eclipse
Definitely the easiest way to edit and run all of the Java code.
#### Installation
* requires java-oracle-8 (needs Java 8 ./jvm/java-8-openjdk-amd64)
* http://ubuntuhandbook.org/index.php/2016/01/how-to-install-the-latest-eclipse-in-ubuntu-16-04-15-10/
* follow this page for remote Eclipse: https://bugs.launchpad.net/ubuntu/+source/libepoxy/+bug/1574886

#### Loading and Editing Project
We modifying the Cornell SPF codebase (https://github.com/clic-lab/spf). A special thank you to them for sharing everything.
Open eclipse `$ECLIPSE [-data WORKSPACE_DIR]`

Add classpath variable<br>
* `Window -> Preferences -> Java -> Build Path -> Classpath Variables`<br>
* new variable named TINY_REPO; set path to **lib/spf<br>**

Switch to workspace that differs from our project folder (e.g. eclipse-workspace).<br>
Import project
  * select root directory (e.g. working-VisionParser) 
  * import dependencies (select lib/spf & select nested directories)

Remove ignore line to run a test case; run as J1test.

### Data
There are two main experiment directories
⋅⋅⋅**visionparser/** (full supervision)
⋅⋅⋅groundedvisionparser (weak supervision that uses the videos and sentence tracker)

The training and test CCG files are identical except the weakly supervised format does not include the ground truth form.

### Setting up sentence tracker
This is only necessary when training the model under weak supervision. If you intend to do a fully supervised run as a benchmark, or to use a saved model for either full or weak supervision, you can skip this step.

This tracker is from [Seeing what you’re told: Sentence-guided activity recognition in video](https://arxiv.org/pdf/1308.4189.pdf) and [A compositional framework for grounding language inference, generation, and acquisition in video](https://www.jair.org/index.php/jair/article/view/10938).

### Running Model

The script `pipeline.sh` builds the necessary Java code and runs a model from start to finish (this includes training and test).
pipeline.sh has many required and optional arguments. Run `pipeline.sh -h` for a detailed description of arguments.

We write (most of) the experiment files from scratch. The **EXPNAME.exp** file is written from scratch. We write a unique file for each fold of the cross-validation, defining the training and test sets.

The libraries and model are recompiled.

sample commands:<br>
`./pipeline.sh -r RUNNAME -e 5 -g 180 -l 180 -v s -o PORT -O 1 -t w`<br>
`./pipeline.sh -d visionparser -r testing -e 5 -g 180 -l 180 -v s -t f`<br>


### Overview of directories
* `experiments`
* `st-for-parser`: sentence tracker