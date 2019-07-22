# DB Model

1. For an app to be deployed these are the mandatory information
```
Tenant > Project > Kubernetes Cluster ID > Kubernetes Cluster Namespace
```
2. `tenant_meta` DB model
- tenant_id
- tenant_display_name
- creation_date
- is_active
3. `project_meta` DB model
- tenant_id
- project_id
- project_display_name
- project_description
- creation_date
- is_active
4. `project_config` DB model
- tenant_id
- project_id
- config_id
- config_type (maven-repository/docker-repository/cloud-aws/kubeconfig)
- config_display_name
- config_file_name
- config_blob
- is_active
- creation_date
- last_updated

# API

1. CRUD tenant
1. CRUD project
2. CRUD project config

# Rules

1. No hard delete. On delete request mark record as inactive.
2. Inter-project cloning supported.
