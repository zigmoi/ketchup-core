# Deployment Module

## DB Model

1. For an app to be deployed these are the mandatory information
```
Tenant > Project > Kubernetes Cluster ID > Kubernetes Cluster Namespace
```
2. `deployment_meta`
- tenant_id
- project_id
- deployment_type (java-internal-service/java-external-service-aws-route53)
- default_config_blob
- is_active
- creation_date
- last_updated
3. `deployment_instance` 
- tenant_id
- project_id
- deployment_id
- deployment_type
- deployment_config_blob
- creation_date
- is_active
- is_running
