# Building and publishing with cloud-native buildpacks

# buildpack in use: cloudfoundry/cnb:bionic
# published to https://hub.docker.com/repositories/triathlonguy 
pack build triathlonguy/todos-api --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-api --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-api -am"
pack build triathlonguy/todos-edge --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-edge --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-edge -am"
pack build triathlonguy/todos-webui --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-webui --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-webui -am"
pack build triathlonguy/todos-mysql --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-mysql --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-mysql -am"
pack build triathlonguy/todos-redis --publish --path . --builder gcr.io/paketo-buildpacks/builder:base --env BP_JAVA_VERSION="8.*" --env BP_BUILT_MODULE=todos-redis --env BP_BUILD_ARGUMENTS="-Dmaven.test.skip=false package -pl todos-redis -am"