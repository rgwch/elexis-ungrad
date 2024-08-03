# elexis-ungrad 2024

**Note**: Beginning with this branch, ungrad-2024, The Elexis Ungrad plugins are built against Branch 3.12 of https://github.com/elexis/elexis-3-core and https://github.com/elexis/elexis-3-base. This branch is considered highly experimental as of now. For produktive use, continue with the ungrad2023 branch for now.


The repositories https://github.com/rgwch/elexis-3-core and https://github.com/rgwch/elexis-3-base are obsolete. 

## What it is
Some more experimental and strictly OpenSource'd extensions from the original creator of Elexis. 



## Build:

Prerequisites: Git, jdk17, Maven 3.6.x; Linux or Windows recommended. MacOS will be a bit tricky,
You'll need also the [elexis-3-core](https://github.com/elexis/elexis-3-core) and [elexis-3-base](https://github.com/elexis/elexis-3-core) already built.

i.e._

```bash
git clone -b 3.12 https://github.com/elexis/elexis-3-core
git clone -b 3.12 https://github.com/elexis/elexis-3-base
git clone -b ungrad-2024 https://github.com/rgwch/elexis-ungrad
cd elexis-ungrad
./build-all.sh

```

You'll find the created repository in `ungrad-p2site/target/repository`. You can use this repository from a running Elexis core via 'Help-Install new Software'.

## Develop

Import the elexis-ungrad projects just the same way you did for the elexis-3-core and elexis-3-base projects in the same workspace. I recommend to create a separate working set "ungrad" for the ungrad projects.

## Contributing

1. Fork this repository
2. Clone your repository to your machine
3. Stay in-sync with upstream.
4. If you've made some changes of general interest, please make a pull request.
