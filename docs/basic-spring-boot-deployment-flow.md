# Basic Spring Boot Deployment Flow

Commands 
- pull-from-remote
- maven-clean-install
- build-spring-boot-docker-image
- deploy-in-kubernetes

### Command - pull-from-remote
```
{
  "command"                               : "pull-from-remote",
  "arg-schema"                            : "v1",
  "args"                                  : [
    {
      "base-path"                         : "/tmp/zigmoi/ketchup/builds/1",
      "git-vendor"                        : "bitbucket",
      "git-vendor-args"                   : {
        "url"                             : "https://btapo@bitbucket.org/gammadev/aws-cicd-autoscale-spring-boot.git",
        "username"                        : "",
        "password"                        : ""
      }
    }
  ]
}
```
