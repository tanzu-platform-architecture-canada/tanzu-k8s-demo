mvn clean install

docker build -t triathlonguy/todos-edge todos-edge/
docker tag triathlonguy/todos-edge triathlonguy/todos-edge:latest
docker push triathlonguy/todos-edge:latest

docker build -t triathlonguy/todos-webui todos-webui/
docker tag triathlonguy/todos-webui triathlonguy/todos-webui:latest
docker push triathlonguy/todos-webui:latest

docker build -t triathlonguy/todos-api todos-api/
docker tag triathlonguy/todos-api triathlonguy/todos-api:latest
docker push triathlonguy/todos-api:latest

docker build -t triathlonguy/todos-redis todos-redis/
docker tag triathlonguy/todos-redis triathlonguy/todos-redis:latest
docker push triathlonguy/todos-redis:latest

docker build -t triathlonguy/todos-mysql todos-mysql/
docker tag triathlonguy/todos-mysql triathlonguy/todos-mysql:latest
docker push triathlonguy/todos-mysql:latest