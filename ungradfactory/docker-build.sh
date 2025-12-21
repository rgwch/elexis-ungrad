#! /bin/bash
# Reproducible build. Products and repositories are built inside the docker container 
# and the resulting p2 repositories are copied to a local build directory.

# First try to exec into existing container (save time to download maven dependencies)
echo "Trying to exec into existing container..."
if docker exec -it elexisfactory /opt/elexisfactory/build.sh; then
    echo "Build completed successfully using existing container."
    exit 0
fi

if(docker ps -a | grep elexisfactory); then
    echo "Running existing stopped container..."
    docker start -ai elexisfactory
    exit 0
fi
# If exec and start both fail, rebuild the docker image and run
echo "Exec failed, rebuilding docker image and starting new container..."
docker build -t rgwch/ungrad-factory:1.1.0 .
docker run -v ./build:/opt/elexisfactory/dist --name elexisfactory rgwch/ungrad-factory:1.1.0 /opt/elexisfactory/build.sh

