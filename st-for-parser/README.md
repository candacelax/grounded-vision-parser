# st-for-parser

To run:

export PATH=/usr/local/cuda-8.0/bin/:$PATH
cd yolo9000
cd darknet 
make

Test darknet with:
./darknet detector test cfg/combine9k.data cfg/yolo9000.cfg ../yolo9000-weights/yolo9000.weights data/horses.jpg

videos have to be in videos/
detector results are cached in videos-cache/

You need ffmpeg, cuda 8, nvcc, cudnn
