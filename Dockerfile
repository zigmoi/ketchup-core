FROM openjdk:8-jre-slim-buster
WORKDIR /app

#install kubectl.
#ADD https://storage.googleapis.com/kubernetes-release/release/v1.19.0/bin/linux/amd64/kubectl /app
#RUN chmod +x ./kubectl
#RUN mv ./kubectl /usr/local/bin/kubectl

#install helm.
ADD https://get.helm.sh/helm-v3.5.0-rc.2-linux-amd64.tar.gz /app
RUN tar -xf helm-v3.5.0-rc.2-linux-amd64.tar.gz
RUN mv ./linux-amd64/helm /usr/local/bin/helm

COPY ./target /app/target
CMD ["/bin/sh", "-c", "java -jar /app/target/*.jar --spring.config.location=/app-config/application.properties"]