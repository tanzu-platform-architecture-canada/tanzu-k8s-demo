# TODO demo execution

1. Setting up a local environment
2. Setting up the demo project
3. Installing pre-requisite tools for running the TODO demo app
4. Installing and running the TODO demo app

## Setting up a local environment

* [JDK 8](https://adoptopenjdk.net/installation.html) or higher. Please ensure you have a JDK installed and not just a JRE
* [Docker](https://docs.docker.com/install/) installed
* [Kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/) installed
* [Helm](https://helm.sh/docs/intro/quickstart/) installed
* Buildpacks
    
    * [Pack CLI](https://buildpacks.io/docs/install-pack/) installed, for building with cloud-native buildpacks - [optional]
    * [KPack CLI](https://github.com/pivotal/kpack/blob/master/docs/install.md) installed, for building with the Kubernetes native cloud-native buildpack - [optional]
* Kubernetes cluster available

    * Use a local Kubernetes cluster via Docker-desktop - [demo works out-of-the-box with a local cluster]
    * Use a public cloud cluster 

## Setting up the demo project

```shell
> git clone git@github.com:tanzu-platform-architecture-canada/tanzu-k8s-demo.git
> cd tanzu-k8s-demo
```

## Installing pre-requisite tools for running the TODO demo app

```shell
> ./install-tools.sh

# Sets up the MariaDB database and the Redis Cach using Bitnami
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install todo-mysql-instance bitnami/mariadb --set rootUser.password=topsecret
helm install todo-redis-instance bitnami/redis --set global.redis.password=topsecret

# Sets up the MariaDB database and the Redis Cach using Tanzu Application Catalog - beta
# helm repo add tac https://charts.trials.tac.bitnami.com/demo
# helm install todo-mysql-instance tac/mariadb --set rootUser.password=topsecret
# helm install todo-redis-instance tac/redis --set global.redis.password=topsecret
```

## Installing and running the TODO demo app

To only install the services within the TODO app and run the demo, without building:
```shell
> deploy-app.sh
```

To build the images and install the services within the TODO app and run the demo:
```shell
> build.sh
> deploy-app.sh
```

To run the demo, it is **recommended** that you start from the edge service, which can be accessed at ```http://localhost:9999/```


To run the demo UI only, without connecting to the services, go to: ```http://localhost:8080/```

