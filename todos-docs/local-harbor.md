# Install Harbor Locally

This document will discuss installing the Harbor container image registry on a workstation.

Requirements:

* This document assumes OSX
* Brew is installed
* [Docker for Desktop for OSX](https://docs.docker.com/docker-for-mac/install/) is installed

## Kind

Kind stands for "Kubernetes in Docker" and is an easy way to get a local Kubernetes cluster running.

*Note: Docker for Desktop also provides Kubernetes functionality, but in this example we are using Kind, which will deploy k8s into Docker*

Install kind with brew.

```
brew install kind
```

Create a kind based cluster.

*Note the use of "extraPortMappings"

```
cat <<EOF | kind create cluster --config=-
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
        authorization-mode: "AlwaysAllow"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
EOF
```

Once that kind cluster is created we can see k8s nodes.

```
$ kubectl get nodes
NAME                 STATUS   ROLES    AGE     VERSION
kind-control-plane   Ready    master   2m31s   v1.17.0
```

## Contour Ingress

[Contour](https://projectcontour.io/) is an advanced open source ingress system supported in part by VMware.

Create Contour ingress:

```
kubectl apply -f https://projectcontour.io/quickstart/contour.yaml
```

Patch it:

```
kubectl patch daemonsets -n projectcontour envoy -p '{"spec":{"template":{"spec":{"nodeSelector":{"ingress-ready":"true"},"tolerations":[{"key":"node-role.kubernetes.io/master","operator":"Equal","effect":"NoSchedule"}]}}}}'
```

Contour should be running.

*Note: kubectl is provided by Docker for Desktop*

```
$ kubectl get pods -n projectcontour
NAME                       READY   STATUS      RESTARTS   AGE
contour-54df6b8854-dlsnr   1/1     Running     0          83s
contour-54df6b8854-m2w8k   1/1     Running     0          83s
contour-certgen-n78dz      0/1     Completed   0          83s
envoy-kwr8x                2/2     Running     0          10s
```

## Install Helm

Getting helm is quite easy:

```
brew install helm
```

## Install Harbor

Now that we have Kubernetes and Helm, installing Harbor is straighforward, though there are many options available in the Helm chart. We will not be making any changes and use the defaults provided.

## Access Harbor

Add a hostname to your `/etc/hosts` file.

```
127.0.0.1 core.harbor.domain
```

Now open a browser session to http://core.harbor.domain/harbor

```
login: admin
password: Harbor12345
```

At this point Harbor is available for local use.