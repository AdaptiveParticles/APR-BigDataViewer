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
./run.sh <your APR file>
```

## Output
Example visualization:
![7GB data set with 4 tiled zebrafishes](doc/screenShot.png/?raw=true)


## Contact us

If anything is not working as you think it should, or would like it to, please get in touch with us!!!

[![Join the chat at https://gitter.im/LibAPR](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/LibAPR/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
