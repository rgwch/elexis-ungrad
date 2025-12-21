# Elexis-Ungrad 2026

**Important**: For users in Switzerland, update is required to be able to use the new Tardoc Tariff. For update instructions, see [here](https://www.elexis.ch/ungrad/reference/upgrade2026/)

**Note**: Beginning with this branch The Elexis Ungrad plugins are built against Branch 3.13 of elexis-3-core and elexis-3-base and require Java 21.

The repositories https://github.com/rgwch/elexis-3-core and https://github.com/rgwch/elexis-3-base can still have some differences to their respective https://github.com/elexis/elexis-3-xx equivalents, but these are rather some small quick fixes and adaptations.  

## What it is
Some more experimental and strictly OpenSource'd extensions from the original creator of Elexis. 

## Download
Link the Repository with the elexis [ungrad plugins](https://www.elexis.ch/ungrad2026/ungrad_2026/ungrad/) with your Elexis installation ("Help/Install new Software"/"Hilfe/Neue Software installieren")

## Build:

### Reproducible build using docker:

```bash
cd ungradfactory
./docker-build.sh
```
Products and Repositories will be in ungradfactory/dist

### Custom build

Prerequisites: Git, jdk21, Maven 3.9.x; Linux or Windows recommended. MacOS will be a bit tricky,

Full build:

```bash
git clone -b ungrad-2026 https://github.com/rgwch/elexis-3-core
git clone -b ungrad-2026 https://github.com/rgwch/elexis-3-base
git clone -b ungrad-2026 https://github.com/rgwch/elexis-ungrad elexis-ungrad-plugins
cd elexis-ungrad-plugins
./build-all.sh

```
You'll find the Elexis programs für Linux, Mac and Windows in `./elexis-3-core/ch.elexis.core.p2site/target/products`. The core repository will be in `./elexis-3-core/ch.elexis.core.p2site/target/repository`, the base repository in `./elexis-3-base/ch.elexis.base.p2site/target/repository`, and the ungrad repository in `./elexis-ungrad-plugins/ch.elexis.ungrad.p2site/target/repository`.
(You can use these repositories from within the running Elexis core instance via 'Help-Install new Software').

## Develop

Elexis 3.13 builds well with Eclipse 2025-09. First, set the compiler and JRE to Java 21. Then, import elexis-3-core into the workspace. Wait until build is finished, then load ch.elexis.target/ide.target. Again, wait until the target is resolved. Then click "Set as current target". Now, the build should complete without errors.
Next, import elexis-3-base and elexis-ungrad into the workspace. I would recommend to create separate workings sets for each import. 

## Contributing

1. Fork this repository
2. Clone your repository to your machine
3. Stay in-sync with upstream.
4. If you've made some changes of general interest, please make a pull request.
