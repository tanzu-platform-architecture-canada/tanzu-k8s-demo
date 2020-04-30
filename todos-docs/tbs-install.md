# Short-form installation of Tanzu Build Service
## Installation on upstream K8s on a local machine

##### Prerequisites
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

## Install Tanzu Build Service
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

## Uninstall Tanzu Build Service
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

### Demo app - CNB SpringBoot demo 
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
```yaml
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

dandobrin@vmwin-008:/tmp:>pb image builds triathlonguy/cnb-demo:master
Build    Status     Started Time           Finished Time          Reason    Digest
-----    ------     ------------           -------------          ------    ------
    1    SUCCESS    2020-04-28 10:20:14    2020-04-28 10:22:58    CONFIG    118895f403f12bbb38f05780a45b3a6846797a7a74422012d85d6e83f6db696f

# Reasons:
    CONFIG: Occurs when a change is made to commit, branch, Git repository, or build fields on the image’s configuration file and you run pb image apply.
    COMMIT: Occurs when new source code is committed to a branch or tag build service is monitoring for changes.
    BUILDER: Occurs when new buildpack versions are made available through an updated builder image.
```