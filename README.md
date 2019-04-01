# Grounded semantic parser

## Installing and Using Eclipse
Definitely the easiest way to edit and run the code.
### Installation
* requires java-oracle-8 (needs Java 8 ./jvm/java-8-openjdk-amd64)
* http://ubuntuhandbook.org/index.php/2016/01/how-to-install-the-latest-eclipse-in-ubuntu-16-04-15-10/
* follow this page for remote Eclipse: https://bugs.launchpad.net/ubuntu/+source/libepoxy/+bug/1574886

### Loading Project in Eclipse
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


## Data
**experiments/visionparser**
**experiments/groundedvisionparser**

## Setting up sentence tracker


## How to Build and Run the code 
To build the code run `ant dist`, and correspponding distribution .jar file will be generated in `dist/` folder. 
To run the experiment `java -jar  dist/VisionParser-1.0.jar <.exp file for Experiment>` (Refer to [Cornell SPF] (https://bitbucket.org/yoavartzi/spf#markdown-header-factored-lexicons) for the detailed explanation about .exp files)

## Modifying the code
Start from understanding .exp file, and read the `mainclass` variable defined in `build.xml` to find main entry point.

## Running Model

The script _pipeline.sh_ runs a model from start to finish (this includes training and test).
pipeline.sh has many required and optional arguments. Run `pipeline.sh -h` for a detailed description of arguments.

We write (most of) the experiment files from scratch. The **EXPNAME.exp** file is written from scratch. We write a unique file for each fold of the cross-validation, defining the training and test sets.

The libraries and model are recompiled.

sample commands:
`./pipeline.sh -r RUNNAME -e 5 -g 180 -l 180 -v s -o PORT -O 1 -t w`
`./pipeline.sh -d visionparser -r testing -e 5 -g 180 -l 180 -v s -t f`