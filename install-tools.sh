# Using Bitnami
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install todo-mysql-instance bitnami/mariadb --set rootUser.password=topsecret
helm install todo-redis-instance bitnami/redis --set global.redis.password=topsecret

# Using Tanzu Application Catalog - beta
# helm repo add tac https://charts.trials.tac.bitnami.com/demo
# helm install todo-mysql-instance tac/mariadb --set rootUser.password=topsecret
# helm install todo-redis-instance tac/redis --set global.redis.password=topsecret
