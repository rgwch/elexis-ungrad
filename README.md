**Note:** This branch ist discontinued. Please refer to the [Ungrad-2024](https://github.com/rgwch/elexis-ungrad/tree/ungrad-2024) branch for more informations.

# elexis-ungrad

Some more experimental and strictly OpenSource'd extensions from the original creator of Elexis. More informations at [www.elexis.ch/ungrad](http://www.elexis.ch/ungrad) 

## Current branches:

* ungrad2022 - Latest stable branch. Will always compile and work
* master - Development branch. Will not always compile and mostly not work flawless



## Build:

Prerequisites: Git, Java-sdk8, Maven 3.6.x; Linux or Windows recommended. MacOS will be a bit tricky,
You'll need also the [elexis-3-core](https://github.com/rgwch/elexis-3-core) and [elexis-3-base](https://github.com/rgwch/elexis-3-core) already built.

i.e._

```bash
git clone -b ungrad2022 elexis-3-core
cd elexis-3-core
./build.sh
cd ..
git clone -b ungrad2022 elexis-3-base
cd elexis-3-base
./build.sh
cd ..
git clone -b ungrad2022 elexis-ungrad
cd elexis-ungrad
./build.sh

```

You'll find the created repository in `ungrad-p2site/target/repository`. You can use this repository from a running Elexis core via 'Help-Install new Software'.

## Develop

Please follow the instructions [here](https://github.com/rgwch/elexis-3-core/blob/develop/readme.md).

Import the elexis-ungrad projects just the same way you did for the elexis-3-core and elexis-3-base projects in the same workspace. I recommend to create a separate working set "ungrad" for the ungrad projects.

## Contributing

1. Fork this repository
2. Clone your repository to your machine
3. Stay in-sync with upstream.
4. If you've made some changes of general interest, please make a pull request.
