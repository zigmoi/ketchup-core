# Ketchup Core
Application to simplify deployment of web applications on kubernetes.

## Install:

#### Prerequisites:
1. Kubernetes cluster, version >= v1.16.0 and <= v1.19.0.
2. Tekton pipelines, installed on cluster, version >= v0.19
3. Helm CLI client, version >= 3
4. Kubectl Client, version >= v1.16.0 and <= v1.19.0.
5. Mysql server, version >= 5.7

#### Prepare Database:
1. Login into mysql and create mysql database schema to store all ketchup data.
`create database ketchupdb;`
2. Switch to ketchup database.
`use ketchupdb;`
3. Create ketchup tables with initial [data](https://github.com/zigmoi/ketchup-core/blob/37386f41aae25d3414db435c9439eaf9271b56aa/src/main/resources/data.sql) .
`source ketchupdb-init.sql`

#### Install via Helm:
1. Create an application.properties file in the current directory to store all ketchup configuration. 
   Sample:
```
token.signing.key=213asdads@ahgsg123@aaa@@hjj
ketchup.base-url=http://localhost:8080/

spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:mysql://localhost:3306/ketchupdb?autoReconnect=true&useSSL=false
spring.datasource.username=mysql-username
spring.datasource.password=mysql-password

spring.datasource.tomcat.max-wait=20000
spring.datasource.tomcat.max-active=50
spring.datasource.tomcat.max-idle=20
spring.datasource.tomcat.min-idle=15

spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQL5Dialect
spring.jpa.properties.hibernate.id.new_generator_mappings = false
spring.jpa.properties.hibernate.format_sql = true
spring.jpa.properties.hibernate.current_session_context_class=org.springframework.orm.hibernate5.SpringSessionContext
spring.jpa.properties.hibernate.ejb.interceptor=org.zigmoi.ketchup.iam.configurations.TenantInterceptor

logging.level.org.zigmoi.ketchup=ERROR

ketchup.tekton-event-sink-api-path=v1-alpha/applications/revisions/pipeline/tekton-events
spring.mvc.async.request-timeout=300000
```

2. Update mysql details in application.properties using following properties.
   a. spring.datasource.url
   b. spring.datasource.username
   c. spring.datasource.password
3. Update token.signing.key property to a random secret value.
4. (Optional) Update Ketchup base URL (ketchup.base-url) property to a URL which can be used to access 
   ketchup application from kubernetes clusters where apps will be deployed.
   Note: use internet accessible domain for CI using cloud git repositories like github and gitlab.
5. Run following helm commands to install ketchup API server:
   applicationProperties variable is set to location of application.properties file.
```
helm repo add ketchup https://zigmoi.github.io/ketchup-helm-repo
helm repo list
helm repo update
helm install ketchup-core ketchup/ketchup-core --set-file applicationProperties=./application.properties
```  
6. Check installation:
    a. Run `helm list`
       This will list ketchup-core as one of the releases.
    b. Run `kubectl get pods`
       This will show one pod for ketchup-core.
7. Run following commands to expose ketchup API server outside cluster.
```
export KETCHUP_API_SERVER_POD_NAME=$(kubectl get pods --namespace default -l "app.kubernetes.io/name=ketchup-core,app.kubernetes.io/instance=ketchup-core" -o jsonpath="{.items[0].metadata.name}")
kubectl port-forward $KETCHUP_API_SERVER_POD_NAME 8097:8097
```
8. Access all API list via swagger UI in the browser using following URL.
`http://localhost:8097/swagger-ui.html`


#### Install [Ketchup UI](https://github.com/zigmoi/ketchup-ui):
1. Create UI configuration file config.js in current directory.
   Sample:
```
window.REACT_APP_API_BASE_URL="http://localhost:8097";
```
2. Update window.REACT_APP_API_BASE_URL property to API server URL accessible outside kubernetes cluster.
3. Run following helm commands to install ketchup API server:
   applicationProperties variable is set to location of config.js file.
```
helm repo add ketchup https://zigmoi.github.io/ketchup-helm-repo
helm repo list
helm repo update
helm install ketchup-ui ketchup/ketchup-ui --set-file applicationProperties=./config.js
```  
3. Check installation:
    a. Run `helm list`
       This will list ketchup-ui as one of the releases.
    b. Run `kubectl get pods`
       This will show one pod for ketchup-ui.
4. Run following commands to expose ketchup UI outside cluster.
```
export KETCHUP_UI_POD_NAME=$(kubectl get pods --namespace default -l "app.kubernetes.io/name=ketchup-ui,app.kubernetes.io/instance=ketchup-ui" -o jsonpath="{.items[0].metadata.name}")
kubectl port-forward $KETCHUP_UI_POD_NAME 8080:80
```
5. Access UI in browser using following URL.
`http://localhost:8080`
6. Login using admin credentials in demo t1.com tenant using following credentials.
username: admin@t1.com 
password: Pass@123


## Build source.

#### Prerequisites:
1. Kubernetes cluster, version >= v1.18.0.
2. Tekton pipelines, installed on cluster, version >= v0.19
3. Helm CLI client, version >= 3
4. Kubectl Client, version >= v1.18.0.
5. Mysql server, version >= 5.7
6. Java JDK, version >= 1.8
7. NodeJs, version >= 10.7.0
8. NPM, version >= 6.2.0
9. Localtunnel, version >= 2.0.0
10. Git, version >= 2.6.0

#### Prepare Database:
1. Login into mysql and create mysql database schema to store all ketchup data.
   `create database ketchupdb;`
2. Switch to ketchup database.
   `use ketchupdb;`
3. Create ketchup tables with initial data.
   `source ketchupdb-init.sql`

#### Compile and Run:
1. Clone the code repo.
   `git clone https://github.com/zigmoi/ketchup-core.git`
2. Update application.properties in src/main/resources folder.   
3. Build and package jar, run following command inside the root directory of project.
   `mvn clean install`
4. Run application.
   `java -jar target/ketchup-core-0.0.1-SNAPSHOT.jar`
5. Access all API list via swagger UI in the browser using following URL.
   `http://localhost:8097/swagger-ui.html`
6. (Optional) Install [Ketchup UI](https://github.com/zigmoi/ketchup-ui).