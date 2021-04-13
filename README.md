# Mery SLP

A library to help players create graphics mods for games in the AoE series, mainly AoE DE.


## Instructions


### Installation

Download the program from the release part of this Github repository.


### Usage

The program consists of 3 executables:

1, MerySLP: Print this README message

2, SLPDecode: Decode the slp files into human readable graphics and metadata

3, SLPEncode: Create slp files from human readable graphics and metadata

By default, the SLPDecoder and the SLPEncoder read all data in the folder data/decoder-input, data/encoder-output and place the processed data in the folder data/decoder-output, data/encoder-output, respectively, the output folders are created if not present.

On Windows, the program structure should be as follow:

<pre>
MerySLP
.
+-- app
+-- data
|   +-- decoder-input
|   +-- decoder-output
|   +-- encoder-input
|   +-- encoder-output
+-- resources
+-- runtime
+-- MerySLP.exe
+-- SLPDecoder.exe
+-- SLPEncoder.exe
</pre>

On Linux, the program structure should be as follow:

<pre>
MerySLP
.
+-- bin
|   +-- MerySLP
|   +-- SLPDecoder
|   +-- SLPEncoder
+-- data
|   +-- decoder-input
|   +-- decoder-output
|   +-- encoder-input
|   +-- encoder-output
+-- lib
+-- resources
</pre>

For more flexible usage, execute the program from terminal, use flag --help for detailed information.


### Output format

For detailed information visit the SLP files specification on the [Openage github repository](https://github.com/SFTtech/openage/blob/master/doc/media/slp-files.md).

Each graphics file consists of several frames and possibly their corresponding secondary frames, which may describe a series of consecutive actions or different possible appearance of a specific object. Each frame consists of 3 channels, the raw channel, from which the colour is drawed as is, the player channel, a grey-scaled representation which will be drawed based on the combination of the pixel brightness and the palette of the player that possesses the drawed object, the outline channel seems not to be used in AoE DE, because objects in the game are not very tall, anyway. The 3 mentioned channel must be of the exact same size, and for each pixel coordinate, there must be at most 1 channel in which the pixel has the alpha value above 0. Each frame has metadata that represent its colour palette, and the coordinate of its centre, which instructs the game where the image should be placed in accordance to its logical position. Each frame has a corresponding secondary frame, this is to instruct the game how to draw the shadow of the objects inside the game, the secondary frame is drawed separately, so it does not need to have the same size and centre coordinate as the main graphics.


## Bugs report

To report a bug or request an enhancement, create an issue in the issue tab of this Github repository.


## For developers


### Requirement

- [Valhalla build of OpenJDK](https://github.com/openjdk/valhalla)
- OpenJDK 16 or higher if you can't build the Hotspot yourself, remove the \_\_primitive\_\_ annotations in the beginning of the source files

