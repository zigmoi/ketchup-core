FROM openjdk:8-jre-slim-buster
WORKDIR /app

#install helm.
ADD https://get.helm.sh/helm-v3.5.0-linux-amd64.tar.gz /app
RUN tar -xf helm-v3.5.0-linux-amd64.tar.gz
RUN mv ./linux-amd64/helm /usr/local/bin/helm

COPY ./target /app/target
CMD ["/bin/sh", "-c", "java -jar /app/target/*.jar --spring.config.location=/app-config/application.properties"]