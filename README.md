# slp-processor

A high performance library to extract SLP files from AOE series, mainly AOE DE, into their graphic components and archive images and meta data back into SLP files.

## Requirement

- [Valhalla build of OpenJDK](https://github.com/openjdk/valhalla)
- OpenJDK 16 or higher if you can't build the Hotspot yourself, remove the \_\_primitive\_\_ annotations in the beginning of the source files
- [lz4](https://github.com/lz4/lz4) compression library, the built shared library file should be placed under src/main/resouces/io/github/merykitty/slpprocessor/common/
- [libpng](http://www.libpng.org/pub/png/libpng.html) library in the system path, configure the build of pngutils.so in the pom.xml file according to the location of libpng.so
