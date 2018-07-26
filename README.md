# LibAPR-BigDataViewer

Viewer app that utilize BigDataViewer to show Adaptive Particle Representation (APR) files. Thanks to reconstruction 'on the fly' it allows to open bigger files that memory available on the machine.

## How to download and build
* clone repository
```
git clone --recurse https://github.com/krzysg/LibAPR-BigDataViewer
```
* build java app
```
cd LibAPR-BigDataViewer
mvn pakcage -Dmaven.test.skip=true -Dmaven.javadoc.skip=true
```
* run app
```
./run.sh <your h5 file>
```
