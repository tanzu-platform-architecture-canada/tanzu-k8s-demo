# Welcome to the Tanzu Kubernetes Demo

## The audience
Developers, Architects and Engineers interested in learning about different aspects of the Tanzu Portfolio

## The evolution
This demo project is considered a living project, which evolves constantly and incorporates the usage of new tools, communication protocols and cloud-native patterns. 

Its documentation shall always reflect the capabilities offered within its modules.

## Why this demo? 
Simply put, three things:

1. A microservices developer playground for all things [Spring Boot](http://spring.io/projects/spring-boot) using the [TODO domain model](http://todomvc.com). The "TODO" model is well understood, concise and consistent, which makes it useful for reasoning about and looking into multiple frameworks, tools and protocols
2. An illustration of how Tanzu Portfolio components help teams address the three main areas of the work: **Build -- Run -- Manage**
3. An educational tool for illustrating **cloud-native patterns** with simple and clear examples

## Table of Contents

#### A. [TODO demo execution](/todos-docs/demo.md)
1. Setting up a local environment
2. Setting up the demo project
3. Installing pre-requisite tools for running the TODO demo app
4. Installing and running the TODO demo app

#### B. [The Tanzu portfolio tools](/todos-docs/tanzu-portfolio.md)

1. Tanzu Mission Control
2. Wavefront for Tanzu platform
3. Cloud-native builds
    - 3.1 [Open-source: Cloud-native Buildpacks and Kpack](/todos-docs/tbs.md)
    - 3.2. [Tanzu Build Service](/todos-docs/tbs-install.md)
4. Tanzu Application Catalog
5. [Harbor Registry](/todos-docs/local-harbor.md)
6. Spring Cloud Config Server

#### C. [The cloud-native patterns](/todos-docs/patterns.md)

1. Application lifecycle
    - 1.1 Blue-green deployments
    - 1.2 Canary deployments
    - 1.3 Rolling updates
2. Observability
3. Security
4. Fronting services
5. Accessing applications
6. App configuration
7. App state
8. Event-driven microservices

#### D. [Developer Aspects](/todos-docs/developer.md)
## What do the modules represent?
This repository contains modules addressing multiple aspects:

* Work together as a **TODO application**
* Work individually as samples, highlighting particular cloud-native patterns built using [Spring Boot](http://spring.io/projects/spring-boot) and [Spring Cloud](https://spring.io/projects/spring-cloud)
* Work natively on the [Tanzu Platform](https://cloud.vmware.com/tanzu) and any other [Kubernetes platform](https://kubernetes.io)
* Is a base application that can be delivered also as part of a **[Modern Application Developer Workshop](/todos-workshop/workshop.md)**

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/todo-services.png "Application Flow")

## The Microservices Playground

### [__todos-edge__](/todos-edge)

TODO(s) Edge is an edge for other TODO apps and serves as a client entry-point into functionality.

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/edge.png "todos-edge")

### [__todos-webui__](/todos-webui)

A sample frontend [Vue.js](https://vuejs.org/) app wrapped in [Spring Boot](https://spring.io/projects/spring-boot)

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/ui.png "todos-ui")

### [__todos-api__](/todos-api)

A sample [Spring Boot](https://spring.io/projects/spring-boot) microservice that uses spring-boot-starter-web to implement a Spring MVC based REST API for TODO(s)

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/rest-api.png "todos-api")

### [__todos-redis__](/todos-redis)

A sample [Spring Boot](https://spring.io/projects/spring-boot) service that uses JPA and Spring Data Redis to cache data.

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/redis.png "todos-redis")

### [__todos-mysql__](/todos-mysql)

A sample [Spring Boot](https://spring.io/projects/spring-boot) service that that provides data persistence in a MySQL DB using JPA.

![alt text](https://github.com/tanzu-platform-architecture-canada/tanzu-k8s-demo/blob/master/images/mysql.png "todos-myswl")