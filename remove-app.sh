# remove deployed microservices
kubectl delete -f todos-mysql/app.yml
kubectl delete -f todos-redis/app.yml
kubectl delete -f todos-api/app.yml
kubectl delete -f todos-webui/app.yml
kubectl delete -f todos-edge/app.yml
