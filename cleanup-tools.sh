# remove installed tools

helm del todo-mysql-instance
helm del todo-redis-instance
kubectl delete pvc data-todo-mysql-instance-mariadb-master-0
kubectl delete pvc data-todo-mysql-instance-mariadb-slave-0
kubectl delete pvc redis-data-todo-redis-instance-master-0
kubectl delete pvc redis-data-todo-redis-instance-slave-0
kubectl delete pvc redis-data-todo-redis-instance-slave-1