# Reproducible build

With this system it should be possible to produce reliable and reproducible builds of Elexis core, Elexis base, and Elexis ungrad.
After a successful build, you'll find the produced repositories and products in the "build" directory.

* **core-p2site**
    * products:  The Elexis executables for all supported OS's
    * repository: The core repository
* **base-p2site**: The base repository. You can load it from Elexis with Help/Install New Software and then install base features from there.
* **ungrad-p2site**: The ungrad repository. You can load it from Elexis with Help/Install New Software and then install ungrad features from there.

## Usage:

`./docker-build.sh`

The first time, this will take a long time and download substantial amounts of data from the internet. Subsequent executions will be faster.
Each call will pull the latest state of the repositories and build them.

