# Tanzu Build Service - installation on any K8s - local env

### A. Tanzu Build Service Setup
* [Pre-requisites](#1)
* [SETUP -  Tanzu Build Service - with a Default install and Builder](#2)

### B. Work with Default Builder

* [USE -  Create a Project and build repositories automatically with the Default Builder](#3)
* [MANAGE -  Update the Build Service Stack from the VMware Tanzu Network - Build Service Dependencies](#6)

### C. Custom Builders
* [CREATE -  Custom Cluster Builder in the Build Service](#4)
* [USE -  Build repositories with Custom Builders](#5)
* [AUTOMATIC UPDATES -  Automatic build updates when buildpacks change](#7)
* [MANAGE -  Updating Store, Stack and Custom Builder resources for an Image](#8)
---

<a name="1"></a>
# Setup 
## Prerequisites
* Install [Docker Desktop](https://hub.docker.com/editions/community/docker-ce-desktop-mac) : You will use this to create your local Kubernetes cluster, as well as to run your containers built by Tanzu Build Service.
* Create a [Docker Hub account](https://hub.docker.com): This is where Tanzu Build Service will push your built images for you to pull down and run. It is also where you will push your Tanzu Build Service build images during the install process.
* Install the [Pivnet CLI](https://github.com/pivotal-cf/pivnet-cli): This is an easy way to download applications and bundles from the [VMware Tanzu Marketplace](https://tanzu.vmware.com/services-marketplace), straight from the command line, without worrying about passing credentials in curl headers. If you would rather, you can modify commands used in this post to use curl, wget, or similar.
* Install the [Kubectl CLI](https://kubernetes.io/docs/tasks/tools/install-kubectl/): This is how you will manage your Kubernetes environment. In addition, Tanzu Build Service uses your Kubernetes config file to connect and authenticate with your local cluster. 
* Install Duffle (either [Open Source](https://github.com/cnabio/duffle) or from the [VMware Tanzu Network](https://network.pivotal.io/products/build-service/)): Duffle is an application for installing and managing distributed applications like Tanzu Build Service. You will use it here to push the Tanzu Build Service installation image to your registry, as well as installing Tanzu Build Service onto Kubernetes. The version from the Tanzu Network is preferred for this project since it is tested with the Tanzu Build Service version deployed. 


#### Prepare the credentials file for installing TBS
```yaml
> cat credentials.yml 
name: credentials
credentials:
 - name: kube_config
   source:
     path: "/Users/dandobrin/.kube/config"
   destination:
     path: "/root/.kube/config"
```

#### Log into Docker
```yaml
> docker login index.docker.io/triathlonguy
```

#### Download the Tanzu Build Service binaries from PivNet
```shell
> pivnet download-product-files --product-slug='build-service' --release-version='0.1.0' --product-file-id=648378
```
#### Download PB CLI from VMware Tanzu Network
```shell
> pivnet download-product-files --product-slug='build-service' --release-version='0.1.0' --product-file-id=648384
```

##### Download Duffle from VMware Tanzu Network
```shell
> pivnet download-product-files --product-slug='build-service' --release-version='0.1.0' --product-file-id=648381
```

#### Relocate image via Duffle
* remove any existing relocated.json beforehand
* run from the /tmp folder - beta version

```shell
> duffle relocate -f ./build-service-0.1.0.tgz -m ./relocated.json -p triathlonguy
```

<a name="2"></a>
## SETUP -  Tanzu Build Service - with a default install and Builder
```shell
> duffle install tbs-deploy-local -c credentials --set kubernetes_env=docker-desktop --set docker_registry=index.docker.io --set docker_repository='triathlonguy'  --set registry_username='triathlonguy'  --set registry_password='ship2020pa'  --set custom_builder_image="triathlonguy/default-builder" -f /tmp/build-service-0.1.0.tgz  -m /tmp/relocated.json -v

> duffle install tbs-deploy-local -c credentials --set kubernetes_env=docker-desktop \ 
        --set docker_registry=index.docker.io \ 
        --set docker_repository='triathlonguy' \ 
        --set registry_username='triathlonguy'  \ 
        --set registry_password='***' \ 
        --set custom_builder_image="triathlonguy/default-builder" \ 
        -f /tmp/build-service-0.1.0.tgz \ 
        -m /tmp/relocated.json -v
```

#### Uninstall Tanzu Build Service
```shell
> duffle uninstall tbs-deploy-local -c credentials --set kubernetes_env=docker-desktop --set docker_registry=index.docker.io --set docker_repository=“index.docker.io/triathlonguy”  --set registry_username=“triathlonguy”  --set registry_password=“ship2020pa”   -m /tmp/relocated.json -v
```

#### Install PB CLI 
```shell
# download binaries if not already downloaded
> pivnet download-product-files --product-slug='build-service' --release-version='0.1.0' --product-file-id=648384

# add PB CLI to the path
> mv pb-0.1.0-darwin pb
> sudo chmod +x pb
> mv pb /usr/local/bin

# Check the list of builders
> pb builder list
```

Note: PB CLI does not work until TBS is installed on the machine

#### Check the builder version
```shell 
> pb version
CLI Version: 0.1.0 (e9b0e13a)
```

<a name="3"></a>
# B. Default Builder
## USE -  Create a Project and build repositories automatically with the Default Builder

#### The Demo app - CNB SpringBoot demo 
This document uses a simple Spring demo application for cloud-native buildpacks, with a simple UI for illustrative purposes.

```shell
> git clone git@github.com:ddobrin/cnb-springboot.git
```

#### Start by creating a project
```shell
# demo project: cnb-demo
> pb project create cnb-demo
Successfully created project 'cnb-demo'
```

#### List the projects set up in TBS
```shell
> pb project list
Projects
--------
cnb-demo
```

#### Target the project
```shell
> pb target project cnb-demo
Successfully set 'cnb-demo' as target. Subsequent commands will assume you are targeting 'cnb-demo'
```

#### Add a couple of users
```shell
> pb project user add triathlonguy
Successfully added user 'triathlonguy' to 'cnb-demo'

> pb project user add ddobrin
Successfully added user 'ddobrin' to 'cnb-demo'
```

#### List all project users
```shell
> pb project members
Project: cnb-demo

Users
-----
ddobrin
docker-for-desktop
triathlonguy

Groups
------
```

#### Configure the DockerHub credentials for the project
```yaml
# dockerhub-config.yml 
project: cnb-demo
registry: https://index.docker.io/v1/
username: triathlonguy
password: *******
```

#### Configure the Github credentials
```yaml
# github-config.yml 
project: cnb-demo
repository: github.com/tanzu-platform-architecture-canada/
username: ddobrin
password: ****
```

Note: do not use a password with anything other than alphanumeric

#### Create the Git credentials in TBS
```shell
> pb secrets git apply -f github-config.yml 
Successfully created git secret for 'github.com/ddobrin' in project 'cnb-demo'
```

#### Create the Dockerhub credentials
```shell
> pb secrets registry apply -f dockerhub-config.yml 
Successfully created registry secret for 'https://index.docker.io/v1/' in project 'cnb-demo'
```

#### Set up image configurations
##### master branch
```yaml
# cnb-demo-config.yml
source:
  git:
    url: https://github.com/ddobrin/cnb-springboot 
    revision: master
image:
  tag: triathlonguy/cnb-demo:master
```

##### develop branch
```yaml
# cnb-demo-config-develop.yml
source:
  git:
    url: https://github.com/ddobrin/cnb-springboot 
    revision: master
image:
  tag: triathlonguy/cnb-demo:develop
```

#### Create the image
```shell
> pb image apply -f cnb-demo-config.yml 
Successfully applied image configuration 'triathlonguy/cnb-demo' in project 'cnb-demo'
```

#### Observe the start of the build process
> pb image builds triathlonguy/cnb-demo:master
Build    Status      Started Time           Finished Time    Reason    Digest
-----    ------      ------------           -------------    ------    ------
    1    BUILDING    2020-04-27 14:07:58    --               CONFIG    --

#### List the images
```shell> pb image list
Project: cnb-demo

Images
------
index.docker.io/triathlonguy/cnb-demo:master
```

#### Observe the finished image
```shell
> pb image builds triathlonguy/cnb-demo
Build    Status     Started Time           Finished Time          Reason    Digest
-----    ------     ------------           -------------          ------    ------
    1    SUCCESS    2020-04-27 14:07:58    2020-04-27 14:11:14    CONFIG    292f6c85268af3b5c499ee49c2433d2ff02afe48b70a47719788818ce12b6629
```

#### Image can be viewed in DockerHub
```shell
https://hub.docker.com/repository/docker/triathlonguy/cnb-demo
```

#### First build labelled b1
```shell
docker pull triathlonguy/cnb-demo:b1.20200427.180758
```

#### Pull it and then start it locally
```shell
> docker run --rm -p 8080:8080/tcp triathlonguy/cnb-demo:master
```

#### Commit a code change to Github - observe the build starting
```shell
> pb image builds triathlonguy/cnb-demo
Build    Status      Started Time           Finished Time          Reason    Digest
-----    ------      ------------           -------------          ------    ------
    1    SUCCESS     2020-04-27 14:07:58    2020-04-27 14:11:14    CONFIG    292f6c85268af3b5c499ee49c2433d2ff02afe48b70a47719788818ce12b6629
    2    BUILDING    2020-04-27 15:30:10    --                     COMMIT    --
```
#### Observe the new image being created 
```shell
> pb image builds triathlonguy/cnb-demo:master
Build    Status     Started Time           Finished Time          Reason    Digest
-----    ------     ------------           -------------          ------    ------
    1    SUCCESS    2020-04-28 10:20:14    2020-04-28 10:22:58    CONFIG    118895f403f12bbb38f05780a45b3a6846797a7a74422012d85d6e83f6db696f
```

#### Image builds can always be introspected
##### Introspect the first build
```shell
> pb image build triathlonguy/cnb-demo:master -b 1
------------------
Retrieving information for image "index.docker.io/triathlonguy/cnb-demo:master" - build 1
------------------
STATUS
     Status:     SUCCESS
     Reasons:    Config
     Image:      index.docker.io/triathlonguy/cnb-demo@sha256:118895f403f12bbb38f05780a45b3a6846797a7a74422012d85d6e83f6db696f
------------------
BUILD DETAILS
     Run Image:  index.docker.io/triathlonguy/tbs-dependencies-run-fa566eed03f50368e2ff40858f61b6d5@sha256:e64856ed89a096486c9bce1590414ccb7d81542bbf56dc68e5477cf09abb3523
     Builder:    triathlonguy/default-builder@sha256:fd01fef73092e1d71342e4802d112b43a0b26e73a721d73a3c11ae52d6d1cbe8

     Source:
         Git:        https://github.com/ddobrin/cnb-springboot
         Revision:   ae0fba980c21bdf8713aec670d18f5d6428dcb62
     Buildpacks: 
         Id:         io.pivotal.openjdk
         Version:    1.2.14

         Id:         io.pivotal.buildsystem
         Version:    1.1.15

         Id:         io.pivotal.jvmapplication
         Version:    1.1.13

         Id:         io.pivotal.tomcat
         Version:    1.3.18

         Id:         io.pivotal.springboot
         Version:    1.3.14

         Id:         io.pivotal.distzip
         Version:    1.1.12

         Id:         io.pivotal.clientcertificatemapper
         Version:    1.1.12

         Id:         io.pivotal.containersecurityprovider
         Version:    1.1.11

         Id:         io.pivotal.springautoreconfiguration
         Version:    1.1.12

------------------
```

## Good practice
#### Set up your automated builds by branches
```shell
> pb image list
Project: cnb-demo

Images
------
index.docker.io/triathlonguy/cnb-demo:master
index.docker.io/triathlonguy/cnb-demo:develop
```

#### Follow the builds
```shell
> pb image builds triathlonguy/cnb-demo:develop
Build    Status     Started Time           Finished Time          Reason    Digest
-----    ------     ------------           -------------          ------    ------
    1    SUCCESS    2020-04-28 10:27:53    2020-04-28 10:30:39    CONFIG    734111f87c528afbc588870c8de6b442bbe4434f3c157d43fedd96484d205c03
    2    SUCCESS    2020-04-28 10:52:41    2020-04-28 10:53:40    COMMIT    5f112339d2687d56dda841e886ce6576a030cdcd5d8c10464d76d7e7d1dae869

> pb image builds triathlonguy/cnb-demo:master
Build    Status     Started Time           Finished Time          Reason    Digest
-----    ------     ------------           -------------          ------    ------
    1    SUCCESS    2020-04-28 10:20:14    2020-04-28 10:22:58    CONFIG    118895f403f12bbb38f05780a45b3a6846797a7a74422012d85d6e83f6db696f

# Reasons:
    CONFIG: Occurs when a change is made to commit, branch, Git repository, or build fields on the image’s configuration file and you run pb image apply.
    COMMIT: Occurs when new source code is committed to a branch or tag build service is monitoring for changes.
    BUILDER: Occurs when new buildpack versions are made available through an updated builder image.
```

<a name="4"></a>
# C. Custom Builders
## CREATE -  Custom Cluster Builder in the Build Service

#### Create a Store resource
The Store provides a collection of buildpacks that can be utilized by Builders. Buildpacks are distributed and added to the store in buildpackages which are docker images.

Build Service ships with curated collection of Tanzu buildpacks for Java, Nodejs, Python, go, PHP, httpd, and Dotnet. It is important to keep these buildpacks up-to-date. Updates to these buildpacks are provided on the Tanzu Network Build Service Dependency page.
```yaml
apiVersion: experimental.kpack.pivotal.io/v1alpha1
kind: Store
metadata:
  name: todos-demo-store
spec:
  sources:
  - image: gcr.io/paketo-buildpacks/adopt-openjdk
  - image: gcr.io/paketo-buildpacks/gradle
  - image: gcr.io/paketo-buildpacks/maven
  - image: gcr.io/paketo-buildpacks/executable-jar
  - image: gcr.io/paketo-buildpacks/apache-tomcat
  - image: gcr.io/paketo-buildpacks/spring-boot
  - image: gcr.io/paketo-buildpacks/dist-zip
  ```

Create the store as a Kubernetes resources and validate the creation 
```shell
> kubectl apply -f todos-k8s/tbs-store.yaml
store.experimental.kpack.pivotal.io/todos-demo-store created

> kubectl get store
NAME                  READY
build-service-store   True <-- store has been created
todos-demo-store      True

> pb store list
```

#### Create a Stack resource
The Stack provides the build and run images for the Cloud Native Buildpack stack that will be used in a builder.

Build Service ships with the org.cloudfoundry.stacks.cflinuxfs3 stack. Updates to this stack are provided on the Tanzu Network Build Service Dependency page.

```yaml
apiVersion: experimental.kpack.pivotal.io/v1alpha1
kind: Stack
metadata:
  name: base-cnb
spec:
  id: "io.buildpacks.stacks.bionic"
  buildImage:
    image: "gcr.io/paketo-buildpacks/build:base-cnb"
  runImage:
    image: "gcr.io/paketo-buildpacks/run:base-cnb"
```

Create the stack as a Kubernetes resources and validate the creation in Kubernetes and the Build Service:
```shell
> kubectl apply -f todos-k8s/tbs-stack.yaml
stack.experimental.kpack.pivotal.io/bionic-stack created

> kubectl get stack
NAME                  READY
base-cnb              True
build-service-stack   True

> pb stack status
Stack ID:    org.cloudfoundry.stacks.cflinuxfs3
Run Image:   docker.io/triathlonguy/tbs-dependencies-run-fa566eed03f50368e2ff40858f61b6d5@sha256:e64856ed89a096486c9bce1590414ccb7d81542bbf56dc68e5477cf09abb3523
Build Image: docker.io/triathlonguy/tbs-dependencies-build-adb6d35d10815f4cc483514bca657e8c@sha256:111ab5e7ab965dc43d839dbc02413f2fcfac784b407d8b4808d9c905080b8d65
```

#### Create a Custom Cluster Builder resource
A builder references the stack and buildpacks that are used in the process of building source code. They “provide” the buildpacks that run against the application and the OS images upon which the application is built and run.

```yaml
apiVersion: experimental.kpack.pivotal.io/v1alpha1
kind: CustomClusterBuilder
metadata:
  name: todos-demo-cluster-builder
spec:
  tag: triathlonguy/custom-builder
  stack: base-cnb
  store: todos-demo-store
  serviceAccountRef:
    name: ccb-service-account
    namespace: build-service
  order:
  - group:
    - id: paketo-buildpacks/adopt-openjdk
    - id: paketo-buildpacks/gradle
      optional: true
    - id: paketo-buildpacks/maven
      optional: true
    - id: paketo-buildpacks/executable-jar
      optional: true
    - id: paketo-buildpacks/apache-tomcat
      optional: true
    - id: paketo-buildpacks/spring-boot
      optional: true
    - id: paketo-buildpacks/dist-zip
      optional: true
```

Create the Custom Cluster Builder resource and validate the creation in Kubernetes and the Build Service:
```shell
> kubectl apply -f todos-k8s/tbs-custom-cluster-builder.yaml
customclusterbuilder.experimental.kpack.pivotal.io/todos-demo-cluster-builder created

> kubectl get ccb
NAME                         LATESTIMAGE                                                                                            READY
default                      triathlonguy/default-builder@sha256:fd01fef73092e1d71342e4802d112b43a0b26e73a721d73a3c11ae52d6d1cbe8   True
todos-demo-cluster-builder   triathlonguy/custom-builder@sha256:1ddc46d180eca10bcd40bfe4fd9ec39126371e2f831e599de2e0459a026c6a92    True

> pb builder list
Cluster Builders
----------------
default
todos-demo-cluster-builder
```

<a name="5"></a>
## USE -  Build repositories with Custom Builders

Images provide a configuration for build service to build and maintain a docker image utilizing Tanzu Buildpacks and custom Cloud Native Buildpacks.

Build Service will monitor the inputs to the image configuration to rebuild the image when the underlying source or buildpacks have changed.


Image definitions allow you to specify the source repository and it's respective branch, as well as any environment paramters of choice.

```yaml
# Demo project - Master branch
source:
  git:
    url: https://github.com/ddobrin/cnb-springboot 
    revision: master
image:
  tag: triathlonguy/cnb-demo:master
builder:
  name: todos-demo-cluster-builder
  kind: CustomClusterBuilder  
  scope: Cluster

# Demo project - develop branch
source:
  git:
    url: https://github.com/ddobrin/cnb-springboot 
    revision: develop
image:
  tag: triathlonguy/cnb-demo:develop
builder:
  name: todos-demo-cluster-builder
  kind: CustomClusterBuilder  
  scope: Cluster

# Demo project - Release branch rel-1.0.0
source:
  git:
    url: https://github.com/ddobrin/cnb-springboot 
    revision: rel-1.0.0
image:
  tag: triathlonguy/cnb-demo:rel-1.0.0
builder:
  name: todos-demo-cluster-builder
  kind: CustomClusterBuilder  
  scope: Cluster
```

Images are added to the Build Service using the ```pb CLI```:
```shell
> pb image add -f cnb-demo-config-master.yml
> pb image add -f cnb-demo-config-develop.yml
> pb image add -f cnb-demo-config-rel-1.0.0.yml

# Validate that the image has been created
> pb image list
Project: cnb-demo

Images
------
index.docker.io/triathlonguy/cnb-demo:master
index.docker.io/triathlonguy/cnb-demo:develop
index.docker.io/triathlonguy/cnb-demo:rel-1.0.0
```

<a name="7"></a>
## AUTOMATIC UPDATES -  Automatic build updates when buildpacks change

The list of configured images:
```shell
> pb image list
Project: cnb-demo

Images
------
index.docker.io/triathlonguy/cnb-demo:master
index.docker.io/triathlonguy/cnb-demo:develop
index.docker.io/triathlonguy/cnb-demo:rel-1.0.0
```

The list of builders registered with the Tanzu Build Service
```shell
> pb builder list
Cluster Builders
----------------
default                     <-- the default builder, from the original install
todos-demo-cluster-builder  <-- the custom cluter builder, configured subsequently in the Build Service
```

Inspecting the Custom Cluster Builder, we can observe the components, including name, stack and buildpack in use:
```yaml
> kubectl get ccb todos-demo-cluster-builder -o yaml

apiVersion: experimental.kpack.pivotal.io/v1alpha1
kind: CustomClusterBuilder
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"experimental.kpack.pivotal.io/v1alpha1","kind":"CustomClusterBuilder","metadata":{"annotations":{},"name":"todos-demo-cluster-builder"},"spec":{"order":[{"group":[{"id":"paketo-buildpacks/adopt-openjdk"},{"id":"paketo-buildpacks/gradle","optional":true},{"id":"paketo-buildpacks/maven","optional":true},{"id":"paketo-buildpacks/executable-jar","optional":true},{"id":"paketo-buildpacks/apache-tomcat","optional":true},{"id":"paketo-buildpacks/spring-boot","optional":true},{"id":"paketo-buildpacks/dist-zip","optional":true}]}],"serviceAccountRef":{"name":"ccb-service-account","namespace":"build-service"},"stack":"base-cnb","store":"todos-demo-store","tag":"triathlonguy/custom-builder"}}
  creationTimestamp: "2020-05-01T18:31:29Z"
  generation: 1
  
  # The name of the custom cluster builder configured in the CustomClusterBuilder resource
  name: todos-demo-cluster-builder    
  resourceVersion: "2564491"
  selfLink: /apis/experimental.kpack.pivotal.io/v1alpha1/customclusterbuilders/todos-demo-cluster-builder
  uid: a0cdadfa-02b1-469c-a397-3a837c8b4bc1
spec:
  order:
  - group:
    - id: paketo-buildpacks/adopt-openjdk
    - id: paketo-buildpacks/gradle
      optional: true
    - id: paketo-buildpacks/maven
      optional: true
    - id: paketo-buildpacks/executable-jar
      optional: true
    - id: paketo-buildpacks/apache-tomcat
      optional: true
    - id: paketo-buildpacks/spring-boot
      optional: true
    - id: paketo-buildpacks/dist-zip
      optional: true
  serviceAccountRef:
    name: ccb-service-account
    namespace: build-service
  stack: base-cnb
  store: todos-demo-store
  tag: triathlonguy/custom-builder
status:
  builderMetadata:
  - id: paketo-buildpacks/adopt-openjdk
    version: 2.3.1
  - id: paketo-buildpacks/apache-tomcat
    version: 1.1.2
  - id: paketo-buildpacks/maven
    version: 1.2.1
  - id: paketo-buildpacks/gradle
    version: 1.1.2
  - id: paketo-buildpacks/spring-boot
    version: 1.5.2
  - id: paketo-buildpacks/executable-jar
    version: 1.2.2
  - id: paketo-buildpacks/dist-zip
    version: 1.2.2
  conditions:
  - lastTransitionTime: "2020-05-06T13:55:47Z"
    status: "True"
    type: Ready
  latestImage: triathlonguy/custom-builder@sha256:0c4effcf9d3c567a54f8eb500db47246a1e1f3eb34e48f6196d1547019e43ef0
  observedGeneration: 1

  # The stack in use for this image
  stack:
    # The Stack ID
    id: io.buildpacks.stacks.bionic

    # The buildpoack and SHA value of the respective buildpack
    runImage: gcr.io/paketo-buildpacks/run@sha256:d70bf0fe11d84277997c4a7da94b2867a90d6c0f55add4e19b7c565d5087206f

```

The image using the CustomClusterBuilder has been built, from the ```rel-1.0.0 branch``` of the repository and all built images can be listed:
```shell
>pb image builds index.docker.io/triathlonguy/cnb-demo:rel-1.0.0
Build    Status     Started Time           Finished Time          Reason        Digest
-----    ------     ------------           -------------          ------        ------
    1    SUCCESS    2020-05-01 15:25:01    2020-05-01 15:29:12    CONFIG        9fd3e63f1ec036d64a95cfe50d7c0ef612c2bc3bc3b8aad7c4cadda0a309ccf4
    2    SUCCESS    2020-05-04 15:45:38    2020-05-04 15:45:57    STACK         b8617622688283ad7a2513dd3b6235960fd227c24cdb1f9541e2251fdd662f30
    3    SUCCESS    2020-05-05 09:54:12    2020-05-05 09:56:14    BUILDPACK     bdabd4926bc72845eed075798a3ecb6ab81486c278d6ce02ec88621f6f7f180b
    4    SUCCESS    2020-05-06 09:55:47    2020-05-06 09:58:04    BUILDPACK+    ee016d5381f4e6b34bbfbeac1f63cfe3454b6c302b430dab1bba227ea5216494
```

Let's inspect the builds:

The original image has been configured at build time to use the ```base``` stack image in Paketo buildpacks ```gcr.io/paketo-buildpacks```, which can be found in [Paketo Buildpacks](https://github.com/paketo-buildpacks/stacks):

The Java version in use is ```paketo-buildpacks/adopt-openjdk version 2.2.1```, which can be found in the [Paketo AdoptOpenJDK Buildpack](https://github.com/paketo-buildpacks/adopt-openjdk) with [Git Tag 2.2.1](https://github.com/paketo-buildpacks/adopt-openjdk/tree/v2.2.1).

```shell
> pb image build index.docker.io/triathlonguy/cnb-demo:rel-1.0.0 -b 1
------------------
Retrieving information for image "index.docker.io/triathlonguy/cnb-demo:rel-1.0.0" - build 1
------------------
STATUS
     Status:     SUCCESS
     Reasons:    Config
     Image:      index.docker.io/triathlonguy/cnb-demo@sha256:9fd3e63f1ec036d64a95cfe50d7c0ef612c2bc3bc3b8aad7c4cadda0a309ccf4
------------------
BUILD DETAILS
     Run Image:  gcr.io/paketo-buildpacks/run@sha256:fd87df6a892262c952559a164b8e2ad1be7655021ad50d520085a19a082cd379
     Builder:    triathlonguy/custom-builder@sha256:1ddc46d180eca10bcd40bfe4fd9ec39126371e2f831e599de2e0459a026c6a92

     Source:
         Git:        https://github.com/ddobrin/cnb-springboot
         Revision:   9fb37040ef7fb7fe8ce0eb0f7a08ebcf82ce7f35
     Buildpacks: 
         Id:         paketo-buildpacks/adopt-openjdk
         Version:    2.2.1

         Id:         paketo-buildpacks/maven
         Version:    1.2.0

         Id:         paketo-buildpacks/executable-jar
         Version:    1.2.1

         Id:         paketo-buildpacks/apache-tomcat
         Version:    1.1.1

         Id:         paketo-buildpacks/spring-boot
         Version:    1.5.1

         Id:         paketo-buildpacks/dist-zip
         Version:    1.2.1

------------------
```

The Tanzu Build Service is tracking any update to the AdoptOpenJDK buildpack and, with any change, build automatically the configured images.

For this example, let's analyze the latest buildpack update at the time of this writing: version 2.3.1, found at [Git Tag 2.3.1](https://github.com/paketo-buildpacks/adopt-openjdk/tree/v2.3.1)

From the CLI, we have found that the latest build has Build #4:
```shell
> pb image builds index.docker.io/triathlonguy/cnb-demo:rel-1.0.0
Build    Status     Started Time           Finished Time          Reason        Digest
-----    ------     ------------           -------------          ------        ------
...
    4    SUCCESS    2020-05-06 09:55:47    2020-05-06 09:58:04    BUILDPACK+    ee016d5381f4e6b34bbfbeac1f63cfe3454b6c302b430dab1bba227ea5216494
```

Let's inspect now the image with Build #4 and vaslidate that the AdoptOpenJDK image is 2.3.1:
```shell
> pb image build index.docker.io/triathlonguy/cnb-demo:rel-1.0.0 -b 4
------------------
Retrieving information for image "index.docker.io/triathlonguy/cnb-demo:rel-1.0.0" - build 4
------------------
STATUS
     Status:     SUCCESS
     Reasons:    Buildpack, Stack
     Image:      index.docker.io/triathlonguy/cnb-demo@sha256:ee016d5381f4e6b34bbfbeac1f63cfe3454b6c302b430dab1bba227ea5216494
------------------
BUILD DETAILS
     Run Image:  gcr.io/paketo-buildpacks/run@sha256:d70bf0fe11d84277997c4a7da94b2867a90d6c0f55add4e19b7c565d5087206f
     Builder:    triathlonguy/custom-builder@sha256:0c4effcf9d3c567a54f8eb500db47246a1e1f3eb34e48f6196d1547019e43ef0

     Source:
         Git:        https://github.com/ddobrin/cnb-springboot
         Revision:   9fb37040ef7fb7fe8ce0eb0f7a08ebcf82ce7f35
     Buildpacks: 
         # The image is now at the expected version 2.3.1
         Id:         paketo-buildpacks/adopt-openjdk
         Version:    2.3.1

         Id:         paketo-buildpacks/maven
         Version:    1.2.1

         Id:         paketo-buildpacks/executable-jar
         Version:    1.2.2

         Id:         paketo-buildpacks/apache-tomcat
         Version:    1.1.2

         Id:         paketo-buildpacks/spring-boot
         Version:    1.5.2

         Id:         paketo-buildpacks/dist-zip
         Version:    1.2.2

------------------
```

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/ccb-buildpack-update.png "Buildpack Updates")

**Note:** The builds have been executed automatically and a new images can be found in Dockerhub, whenever the Buildpack has changed, from the intial build with version (2.2.1) to build #3 with image (2.3.0) and finally with Build #4 with image (2.3.1).

<a name="8"></a>
## MANAGE -  Updating Store, Stack and Custom Builder resources for an Image

The steps provided in this chapter illustrate how to:
- update an existing stack, store or custom cluster builder
- create additional ones, for parallel or regression testing of a source code repository

As an example, we wish to show how we can update the buildpack to use [Paketo BellSoft Liberica cloud-native Java buildpack](https://github.com/paketo-buildpacks/bellsoft-liberica): ```gcr.io/paketo-buildpacks/bellsoft-liberica```

We modify the store definition as follows:
```yaml
# tbs-store-bellsoft.yaml
apiVersion: experimental.kpack.pivotal.io/v1alpha1
kind: Store
metadata:
  name: todos-demo-store-bellsoft   # <-- the new Store name
spec:
  sources:
  # Removed - - image: gcr.io/paketo-buildpacks/adopt-openjdk
  # Add
  - image: gcr.io/paketo-buildpacks/bellsoft-liberica
  - image: gcr.io/paketo-buildpacks/gradle
  - image: gcr.io/paketo-buildpacks/maven
  - image: gcr.io/paketo-buildpacks/executable-jar
  - image: gcr.io/paketo-buildpacks/apache-tomcat
  - image: gcr.io/paketo-buildpacks/spring-boot
  - image: gcr.io/paketo-buildpacks/dist-zip
```

We give the store new metadata, to indicate in the new configuration that we are using a new buildpack: ```todos-demo-store-bellsoft```. While we can update the previous Store YAML config file, we choose to give it a new resource, to allow for easier testing.
```shell
> kubectl apply -f todos-k8s/tbs-store-bellsoft.yaml 
store.experimental.kpack.pivotal.io/todos-demo-store-bellsoft created

# validate the Store resources created in K8s
> kubectl get store
NAME                        READY
build-service-store         True
todos-demo-store            True
todos-demo-store-bellsoft   True
```

We can now choose to update the existing/create a new Stack resource for using the ```Bellsoft-Liberica``` buildpack:
```yaml
# tbs-stack-bellsoft.yaml
apiVersion: experimental.kpack.pivotal.io/v1alpha1
kind: Stack
metadata:
  name: bellsoft-liberica    # <-- the new Stack name
spec:
  id: "io.buildpacks.stacks.bionic"
  buildImage:
    image: "gcr.io/paketo-buildpacks/build:base-cnb"
  runImage:
    image: "gcr.io/paketo-buildpacks/run:base-cnb"
```

The new Stack is created using the same kubectl CLI:
```shell
> kubectl apply -f todos-k8s/tbs-stack-bellsoft.yaml 
stack.experimental.kpack.pivotal.io/bellsoft-liberica created

> kubectl get stack
NAME                  READY
base-cnb              True
bellsoft-liberica     True
build-service-stack   True
```

At this time, we can update/create a new Custom Cluster Builder, leveraging the ```Bellsoft-Liberica``` buildpack:
```yaml
# tbs-custom-cluster-builder-bellsoft.yaml
apiVersion: experimental.kpack.pivotal.io/v1alpha1
kind: CustomClusterBuilder
metadata:
  name: todos-demo-cluster-bellsoft-builder   # <-- the name of the new custom cluster builder
spec:
  tag: triathlonguy/custom-bellsoft-builder
  stack: bellsoft-liberica   # <-- the new stack name
  store: todos-demo-store-bellsoft   # <-- the new store name
  serviceAccountRef:
    name: ccb-service-account
    namespace: build-service
  order:
  - group:
    - id: paketo-buildpacks/bellsoft-liberica  # <-- the buildpack we wish to use
    - id: paketo-buildpacks/gradle
      optional: true
    - id: paketo-buildpacks/maven
      optional: true
    - id: paketo-buildpacks/executable-jar
      optional: true
    - id: paketo-buildpacks/apache-tomcat
      optional: true
    - id: paketo-buildpacks/spring-boot
      optional: true
    - id: paketo-buildpacks/dist-zip
      optional: true
```

The CustomClusterBuilder is created from the kubectl CLI:
```shell
> kubectl apply -f todos-k8s/tbs-custom-cluster-builder-bellsoft.yaml
customclusterbuilder.experimental.kpack.pivotal.io/todos-demo-cluster-bellsoft-builder created

> kubectl get ccb
NAME                                  LATESTIMAGE                                                                                                    READY
default                               triathlonguy/default-builder@sha256:fd01fef73092e1d71342e4802d112b43a0b26e73a721d73a3c11ae52d6d1cbe8           True
todos-demo-cluster-bellsoft-builder   triathlonguy/custom-bellsoft-builder@sha256:44f4d24b0e164a3425384b5f556dbfceba584c0c326393fd94daa6a49cebd362   True
todos-demo-cluster-builder            triathlonguy/custom-builder@sha256:85e0611700b1ee95137cd0239c5d509b49e04d7f964056f0c84ff57048527341            True

# The Build service recognizes the new builder and adds it to the list of builders
> pb builder list
Cluster Builders
----------------
default
todos-demo-cluster-bellsoft-builder
todos-demo-cluster-builder
```

We can now build images using the new ```Bellsoft-Liberica``` buildpack. To start we create a new Image resource:
```yaml
# tbs-image-bellsoft-branch.yaml
source:
  git:
    url: https://github.com/ddobrin/cnb-springboot 
    revision: rel-bellsoft
image:
  tag: triathlonguy/cnb-demo:rel-bellsoft
builder:
  name: todos-demo-cluster-bellsoft-builder
  kind: CustomClusterBuilder  
  scope: Cluster
```

The Image is created from the ```pb CLI```:
```shell
> pb image apply -f todos-k8s/tbs-image-bellsoft-branch.yaml

# validate that the image has been created
> pb image list
Project: cnb-demo

Images
------
index.docker.io/triathlonguy/cnb-demo:master
index.docker.io/triathlonguy/cnb-demo:develop
index.docker.io/triathlonguy/cnb-demo:rel-1.0.0
index.docker.io/triathlonguy/cnb-demo:rel-bellsoft

# Let's check on the status of the build
> pb image builds index.docker.io/triathlonguy/cnb-demo:rel-bellsoft
Build    Status      Started Time           Finished Time    Reason    Digest
-----    ------      ------------           -------------    ------    ------
    1    BUILDING    2020-05-08 10:13:45    --               CONFIG    --

> pb image status index.docker.io/triathlonguy/cnb-demo:rel-bellsoft
Image
-----
Status:          BUILDING
Message:         N/A
Latest Image:    N/A

Last Successful Build
---------------------
ID:        N/A
Reason:    N/A

Last Failed Build
-----------------
ID:        N/A
Reason:    N/A

# after a few minutes, the first build is complete. 
# subsequents builds are much faster due to caching 
> pb image status index.docker.io/triathlonguy/cnb-demo:rel-bellsoft
Image
-----
Status:          READY
Message:         N/A
Latest Image:    index.docker.io/triathlonguy/cnb-demo@sha256:d220373011ae773ec6588a6354de31eaf3418648df5f9dbe88bd827acdd295e2

Last Successful Build
---------------------
ID:        1
Reason:    CONFIG

Last Failed Build
-----------------
ID:        N/A
Reason:    N/A


>pb image builds index.docker.io/triathlonguy/cnb-demo:rel-bellsoft
Build    Status     Started Time           Finished Time          Reason    Digest
-----    ------     ------------           -------------          ------    ------
    1    SUCCESS    2020-05-08 10:13:45    2020-05-08 10:21:20    CONFIG    d220373011ae773ec6588a6354de31eaf3418648df5f9dbe88bd827acdd295e2
```

The buildpack can be inspected, it is the first build, and we can observe that the bellsofot-liberica buildpack has been used:
```shell
> pb image build index.docker.io/triathlonguy/cnb-demo:rel-bellsoft -b 1
------------------
Retrieving information for image "index.docker.io/triathlonguy/cnb-demo:rel-bellsoft" - build 1
------------------
STATUS
     Status:     SUCCESS
     Reasons:    Config
     Image:      index.docker.io/triathlonguy/cnb-demo@sha256:d220373011ae773ec6588a6354de31eaf3418648df5f9dbe88bd827acdd295e2
------------------
BUILD DETAILS
     Run Image:  gcr.io/paketo-buildpacks/run@sha256:d70bf0fe11d84277997c4a7da94b2867a90d6c0f55add4e19b7c565d5087206f
     Builder:    triathlonguy/custom-bellsoft-builder@sha256:44f4d24b0e164a3425384b5f556dbfceba584c0c326393fd94daa6a49cebd362

     Source:
         Git:        https://github.com/ddobrin/cnb-springboot
         Revision:   ae0fba980c21bdf8713aec670d18f5d6428dcb62
     Buildpacks: 
         Id:         paketo-buildpacks/bellsoft-liberica
         Version:    2.5.2

         Id:         paketo-buildpacks/maven
         Version:    1.2.1

         Id:         paketo-buildpacks/executable-jar
         Version:    1.2.2

         Id:         paketo-buildpacks/apache-tomcat
         Version:    1.1.2

         Id:         paketo-buildpacks/spring-boot
         Version:    1.5.2

         Id:         paketo-buildpacks/dist-zip
         Version:    1.2.2

------------------

```

# Bellsoft-liberica buildpack is updated 

When the buildpack is updated in the image repository, for example from version 2.5.2 to version 2.5.3, the Tanzu Build Service starts the build process automatically, a new image is being created, we can inspect the it and observe that the version of bellsoft-liberica is at version 2.5.3:
```shell
> pb image build index.docker.io/triathlonguy/cnb-demo:rel-bellsoft -b 2
------------------
Retrieving information for image "index.docker.io/triathlonguy/cnb-demo:rel-bellsoft" - build 2
------------------
STATUS
     Status:     SUCCESS
     Reasons:    Buildpack
     Image:      index.docker.io/triathlonguy/cnb-demo@sha256:339692da4d0d6b0bb4c3466f9cb90200e0e5e298f025ba596e282b92bc565eea
------------------
BUILD DETAILS
     Run Image:  gcr.io/paketo-buildpacks/run@sha256:d70bf0fe11d84277997c4a7da94b2867a90d6c0f55add4e19b7c565d5087206f
     Builder:    triathlonguy/custom-bellsoft-builder@sha256:b110b0343ed952719e6a13f3366519f717d3cdf151e9af66405fa3887581d5a5

     Source:
         Git:        https://github.com/ddobrin/cnb-springboot
         Revision:   ae0fba980c21bdf8713aec670d18f5d6428dcb62
     Buildpacks: 
         Id:         paketo-buildpacks/bellsoft-liberica
         Version:    2.5.3

         Id:         paketo-buildpacks/maven
         Version:    1.2.2

         Id:         paketo-buildpacks/executable-jar
         Version:    1.2.3

         Id:         paketo-buildpacks/apache-tomcat
         Version:    1.1.3

         Id:         paketo-buildpacks/spring-boot
         Version:    1.5.3

         Id:         paketo-buildpacks/dist-zip
         Version:    1.3.0

------------------
```

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/ccb-buildpack-update-bellsoft.png "Buildpack Updates")
