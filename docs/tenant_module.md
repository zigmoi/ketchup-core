# DB Model

1. For an app to be depoyed these are the mandatory information
```
Tenant > Kubernetes Cluster ID > Kubernetes Cluster Namespace
```
2. `tenant_master` DB model
- tenant_id
- tenant_display_name
- creation_date
- is_active
3. `tenant_config` DB model
- tenant_id
- config_id
- config_type (repository/cloud-aws/kubeconfig)
- config_display_name
- config_file_name
- config_blob
- is_active
- creation_date
- last_updated

# API

1. CRUD tenant
2. CRUD tenant config
```
No hard delete. On delete request mark record as inactive. 
```
