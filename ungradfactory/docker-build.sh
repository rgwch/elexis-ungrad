#! /bin/bash
# Reproducible build. Products and repositories are built inside the docker container 
# and the resulting p2 repositories are copied to a local build directory.

# First try to exec into existing container
echo "Trying to exec into existing container..."
if docker exec -it elexisfactory /opt/elexisfactory/build.sh; then
    echo "Build completed successfully using existing container."
    exit 0
fi

# If exec fails, rebuild the docker image and run
echo "Exec failed, rebuilding docker image and starting new container..."
docker build -t rgwch/ungrad-factory:1.0.0 .
docker run --rm -v ./build:/opt/elexisfactory/dist --name elexisfactory rgwch/ungrad-factory:1.0.0 /opt/elexisfactory/build.sh


