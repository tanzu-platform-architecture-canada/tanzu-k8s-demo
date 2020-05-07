# Open-source: Cloud-native Buildpacks and Kpack

## Build multi-module repositories from source code:
1. [Building with ```cloud-native buildpacks```](#1)
2. [Building with ```kpacks```](#2)

<a name="1"></a>
## Building with ```cloud-native buildpacks```

1. Install buildpacks

[Download and install the `pack` CLI](https://github.com/buildpacks/pack/releases).
You'll need a Docker daemon running to build container images.

If you run `pack` CLI for the first time, you need to set a default
builder:
```bash
> pack set-default-builder gcr.io/paketo-buildpacks/builder:base
Builder Paketo: gcr.io/paketo-buildpacks/builder:base is now the default builder
```

You may select one of the suggested builders:
```bash
> pack suggest-builders

Suggested builders:
	Cloud Foundry:     cloudfoundry/cnb:bionic         Ubuntu bionic base image with buildpacks for Java, NodeJS and Golang
	Cloud Foundry:     cloudfoundry/cnb:cflinuxfs3     cflinuxfs3 base image with buildpacks for Java, .NET, NodeJS, Python, Golang, PHP, HTTPD and NGINX
	Cloud Foundry:     cloudfoundry/cnb:tiny           Tiny base image (bionic build image, distroless run image) with buildpacks for Golang
	Heroku:            heroku/buildpacks:18            heroku-18 base image with buildpacks for Ruby, Java, Node.js, Python, Golang, & PHP

Tip: Learn more about a specific builder with:
> pack inspect-builder [builder image]
```

You're now ready to use CNB's.

2. Build images with cloud-native buildpacks

The following script allows building multiple individual modules from within the same repository, without the use of Dockerfiles
```shell
> ./build-paketo.sh

# Building and publishing with cloud-native buildpacks

# buildpack in use: gcr.io/paketo-buildpacks/builder:base
# published to https://hub.docker.com/repositories/triathlonguy 

# where:
# image --> the name of the image you wish to build 
#           (ex.:triathlonguy/message-service:blue 
#                org=triathlonguy, 
#                image name=message-service, 
#                tag=blue)
# path --> the path of the repository (ex. current path . , hosts the parent POM file)
# builder --> the name of the builder to use, if not set as default (ex. cloudfoundry/cnb:bionic)
# publish --> indicates whether the image will be published to the repository
# env variables:
#   BP_JAVA_VERSION --> 8.* (11.*) is the default
#   BP_BUILT_MODULE --> the module to build (ex. message-service)
#   BP_BUILD_ARGUMENTS --> build arguments 
#       pl --> Comma-delimited list of specified reactor projects to build instead
#                of all projects. A project can be specified by [groupId]:artifactId
#                   or by its relative path
#       am --> If project list is specified, also build projects required by the listpack build triathlonguy/todos-api --publish --path . --builder cloudfoundry/cnb:bionic --env BP_BUILT_MODULE=todos-api --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-api -am"

pack build triathlonguy/todos-api --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-api --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-api -am"
pack build triathlonguy/todos-edge --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-edge --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-edge -am"
pack build triathlonguy/todos-webui --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-webui --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-webui -am"
pack build triathlonguy/todos-mysql --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-mysql --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-mysql -am"
pack build triathlonguy/todos-redis --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-redis --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-redis -am"
```

<a name="2"></a>
## Building with ```kpack```

1. Install kpack

Download the [latest kpack release](https://github.com/pivotal/kpack/releases): you should have a ```file release-<version>.yaml```. Please note that the file is available in the ```Assets``` section of the kpack release.

Deploy kpack using kubectl:
```shell
> kubectl apply -f todos-k8s/release-<version>.yaml

# ex.: release.-0.0.8.yaml 
# https://github.com/pivotal/kpack/releases/download/v0.0.8/release-0.0.8.yaml

# Validate that kpack is running
> kubectl -n kpack get pods

NAME                                READY   STATUS    RESTARTS   AGE
kpack-controller-5756f5b65b-f6rnn   1/1     Running   0          3d
kpack-webhook-7884b8f45b-7d22r      1/1     Running   0          24h
```

2. First, we create a builder resource for Docker images in K8s: 
```yaml
# cnb-builder.yaml

apiVersion: build.pivotal.io/v1alpha1
kind: ClusterBuilder
metadata:
  name: default
spec:
  image: gcr.io/paketo-buildpacks/builder:base # previously: cloudfoundry/cnb:bionic
```

Create it by running:
```
> kubectl apply -f todos-k8s/cnb-builder.yaml

# Validate that it is running:
> kubectl get clusterbuilder

NAME      LATESTIMAGE                                                                                                READY
default   gcr.io/paketo-buildpacks/builder@sha256:baaf85bc39cb43e364630625590c13b921b7bcfbfd4b30c6d8dfabd56024e6a5   True

# Previously:
NAME      LATESTIMAGE                                                                                                READY
default   index.docker.io/cloudfoundry/cnb@sha256:baaf85bc39cb43e364630625590c13b921b7bcfbfd4b30c6d8dfabd56024e6a5   True
```

2. Second, we create a Secret for DockerHub credentials; you can substitute the credentials and repository of your choice (Docker, Harbor, etc.)
```
# dockerhub-creds.yml
apiVersion: v1
kind: Secret
metadata:
  name: dockerhub-creds
  annotations:
    build.pivotal.io/docker: https://index.docker.io/v1/
type: kubernetes.io/basic-auth
stringData:
  username: <user>
  password: <pwd>
```
Create it by running:
```
> kubectl apply -f dockerhub-creds.yml

# validate that the secret has been created
> kubectl get secrets
NAME                                        TYPE                                  DATA   AGE
...
dockerhub-creds                             kubernetes.io/basic-auth              2      34s
...
```

3. Third, we create a Secret for Github credentials:
```
# github-creds.yml
apiVersion: v1
kind: Secret
metadata:
  name: github-creds
  annotations:
    build.pivotal.io/git: https://github.com
type: kubernetes.io/basic-auth
stringData:
  username: <user>
  password: <password> or <access token for 2FA enabled accounts>
```
Create it by running:
```
> kubectl apply -f todos-k8s/github-creds.yaml

# validate that the secret has been created
> kubectl get secret
NAME                                        TYPE                                  DATA   AGE
dockerhub-creds                             kubernetes.io/basic-auth              2      3m2s
github-creds                                kubernetes.io/basic-auth              2      4s
```

4. Before creating an image resource for building the Docker image for the service, we need to create a service account tying together all the required credentials

```
# kpack-service-account.yml 
apiVersion: v1
kind: ServiceAccount
metadata:
  name: kpack-service-account
secrets:
  - name: dockerhub-creds
  - name: github-creds
```
Create it by running:
```
> kubectl apply -f todos-k8s/kpack-service-account.yaml 

# validate that the service account has been created
>kubectl get sa
NAME                    SECRETS   AGE
default                 1         30d
kpack-service-account   3         10s
spring-cloud            1         30d
```
5. Finally, we need to create the Image resource for building the Docker image
```
# app-source-todos-api
apiVersion: build.pivotal.io/v1alpha1
kind: Image
metadata:
  name: todos-api 
spec:
  tag: triathlonguy/todos-api:blue # --> Set your Docker image
  serviceAccount: kpack-service-account
  builder:
    name: default
    kind: ClusterBuilder
  cacheSize: "1Gi"
  source:
    git:
      url: https://github.com/ddobrin/bluegreen-deployments-k8s # --> set the repo and branch from which to build
      revision: master
  build:
    env:
      - name: BP_JAVA_VERSION
        value: 8.*     # --> Java 11 is used by default, you can set the Java version you require
      - name: BP_BUILT_MODULE # --> set the module to be built 
        value: message-service
      - name: BP_BUILD_ARGUMENTS # --> set the Maven build arguments
        value: "-Dmaven.test.skip=true package -pl message-service -am"

# where env variables to be set are:
#   BP_BUILT_MODULE --> the module to build (ex. message-service)
#   BP_BUILD_ARGUMENTS --> build arguments 
#       pl --> Comma-delimited list of specified reactor projects to build instead
#                of all projects. A project can be specified by [groupId]:artifactId
#                   or by its relative path
#       am --> If project list is specified, also build projects required by the list
```

Create the Image resource:
```
> kubectl apply -f todos-k8s/app-source-todos-api.yaml
```

The image build oricess can be observed using the [logs utility](https://github.com/pivotal/kpack/blob/master/docs/logs.md):
```
./logs -image todos-api
./logs -image todos-webui
```


When the image is built, it can be found in:
```
> kubectl get images

NAME        LATESTIMAGE                                                                                                        READY
todos-api   index.docker.io/triathlonguy/todos-api@sha256:d3d846f7052eb02e75cf375b7ee044024bb5d66653524dc57319b7c1c7063c1b     True
todos-webui index.docker.io/triathlonguy/todos-webui@sha256:6f7d81929a95ce1cb7eddfe5894a98ecfeaa09be8aa6210a8a01d587fba0d9ed   True
```
