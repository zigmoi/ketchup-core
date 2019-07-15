# ketchup-core

Spring boot based backend for ketchup.

# Project has following structure:

ketchup-core
	configuration
	service1
		configuration
		entities
		dtos
		repositories
		services
		controllers
	service2
		configuration
		entities
		dtos
		repositories
		services
		controllers
	service3
	...
	...

Top level configuration package for holding project level configs.
Followed by functionality divided into services, each service will have its own configuration, entities, dtos, repositories, services, controllers and exceptions.

# Note

All request response should use dtos instead of directly using entity classes.
All functionality should use services for core logic while keeping controllers with minimal parsing and validation logic. 
All multi repository write calls should be transactional and should be implemented as a service rather than implementing in controllers using repositories.



# Database

H2DB is file based and is created in home directory of user with name ketchupdb.
h2db console is not accessible via spring boot server due to security reasons, you can use intellij data explorer or any other suitable db explorer to connect to h2db directly by providing the db file location.


# IAM

Project uses spring security oauth2 for oauth2 based identity and access management.

Default flow enabled is Resource owner password flow.
Default password encoder is bcrypt encoder with strength 4.

Default oauth application/client credentials:
client id: client-id-1
client secret: client-id-1-secret

Default application user credentials
username: sa
password: Pass@123

Command used to generate jwt keystore for oauth2 jwt:
```keytool -genkeypair -alias ketchupjwt -keyalg RSA -keypass pass123 -keystore ketchupjwt.jks -storepass pass123```
