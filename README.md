# Elexis-Ungrad 2024

**Note**: Beginning with this branch, ungrad-2024, The Elexis Ungrad plugins are built against Branch 3.12 of elexis-3-core and elexis-3-base. 

The repositories https://github.com/rgwch/elexis-3-core and https://github.com/rgwch/elexis-3-base can still have some differences to their respective https://github.com/elexis/elexis-3-xx equivalents, but these are rather some small quick fixes and adaptations.  

## What it is
Some more experimental and strictly OpenSource'd extensions from the original creator of Elexis. 

## Download
Link the Repository with the elexis [ungrad plugins](https://elexis.ch/ungrad2024/3.12/ungrad/) with your Elexis installation ("Help/Install new Software"/"Hilfe/Neue Software installieren")

## Build:

Prerequisites: Git, jdk17, Maven 3.6.x; Linux or Windows recommended. MacOS will be a bit tricky,

Full build:

```bash
git clone -b 3.12 https://github.com/rgwch/elexis-3-core
git clone -b 3.12 https://github.com/rgwch/elexis-3-base
git clone -b ungrad-2024 https://github.com/rgwch/elexis-ungrad elexis-ungrad-plugins
cd elexis-ungrad-plugins
./build-all.sh

```
You'll find the Elexis programs für Linux, Mac and Windows in `./elexis-3-core/ch.elexis.core.p2site/target/products`. The core repository will be in `./elexis-3-core/ch.elexis.core.p2site/target/repository`, the base repository in `./elexis-3-base/ch.elexis.base.p2site/target/repository`, and the ungrad repository in `./elexis-ungrad-plugins/ch.elexis.ungrad.p2site/target/repository`.
(You can use these repositories from within the running Elexis core instance via 'Help-Install new Software').

## Develop

Elexis 3.12 builds well with Eclipse 2024-06. First, set the compiler and JRE to Java 17. Then, import elexis-3-core into the workspace. Wait until build is finished, then load ch.elexis.target/ide.target. Again, eait until the target is resolved. Then click "Set as current target". Now, the build should complete without errors.
Next, import elexis-3-core and elexis-ungrad into the workspace. I would recommend to create separate workings sets for each import. 

## Contributing

1. Fork this repository
2. Clone your repository to your machine
3. Stay in-sync with upstream.
4. If you've made some changes of general interest, please make a pull request.
