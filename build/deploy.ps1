param([string] $version)

docker build -t "kmruiz/sonata:$version" -f Dockerfile .
docker build -t "kmruiz/sonata:latest" -f Dockerfile .

docker build -t "kmruiz/sonata:playground-$version" -f Dockerfile_playground .
docker build -t "kmruiz/sonata:playground" -f Dockerfile_playground .

docker push "kmruiz/sonata:$version"
docker push "kmruiz/sonata:latest"
docker push "kmruiz/sonata:playground-$version"
docker push "kmruiz/sonata:playground"