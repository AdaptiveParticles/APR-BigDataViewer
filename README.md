# APR-BigDataViewer

Viewer app that utilize [BigDataViewer](https://github.com/bigdataviewer/bigdataviewer-vistools) and [LibAPR-java-wrapper](https://github.com/krzysg/LibAPR-java-wrapper) to show Adaptive Particle Representation (APR) files. Thanks to reconstruction 'on the fly' it allows to open bigger files that memory available on the machine.

## How to download and build
* clone repository
```
git clone --recurse https://github.com/AdaptiveParticles/APR-BigDataViewer.git
```
* build java app
```
cd APR-BigDataViewer
mvn package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true
```
* run app
```
./run.sh <your APR file>
```

## Current branches
### master
Utilizing single-threaded version of LibAPR java wrapper. Using default available compiler.
### openmpEnabled
Using OpenMP version of LibAPR java wrapper. Currently supporting llvm on MacOS, if you need to use different compiler please update CC/CXX/LDFLAGS/CPPFLAGS in submodule file:
LibAPR-java-wrapper/native/cppbuild.sh

## Output
Example visualization:
![7GB data set with 4 tiled zebrafishes](doc/screenShot.png/?raw=true)


## Contact us

If anything is not working as you think it should, or would like it to, please get in touch with us!!!

[![Join the chat at https://gitter.im/LibAPR](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/LibAPR/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
