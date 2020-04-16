# deploy microservices

kubectl apply -f todos-mysql/app.yml
kubectl apply -f todos-redis/app.yml
kubectl apply -f todos-api/app.yml
kubectl apply -f todos-webui/app.yml
kubectl apply -f todos-edge/app.yml