package org.zigmoi.ketchup.iam.configurations;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.exceptions.CrossTenantOperationException;
import org.zigmoi.ketchup.iam.exceptions.UserConfigurationException;

import java.io.Serializable;

public class TenantInterceptor extends EmptyInterceptor {

    @Override //called before insert
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return addTenantId(entity, state, propertyNames, id);
    }

    @Override //called before update
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        checkCrossTenantOperation(entity, currentState, propertyNames, id);
        return false;
    }

    @Override //called before delete
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        checkCrossTenantOperation(entity, state, propertyNames, id);
    }

    private boolean addTenantId(Object entity, Object[] state, String[] propertyName, Serializable id) {
        //Id can be accessed but cannot be modified, doesnt work even if its updated.
        if (entity instanceof Tenant) {
            return false;
        } else if (entity instanceof User) {
            //Allow root tenant to create users in any tenant.
            //Update tenantId attribute if null.
            //Else check if tenant id in qualified name and tenantId attribute in entity are same.

            for (int index = 0; index < propertyName.length; index++) {
                if (propertyName[index].equals("tenantId")) {
                    String currentTenantId = AuthUtils.getCurrentTenantId();
                    String rootTenantId = "zigmoi.com";
                    String userName = id.toString();

                    String tenantIdInQualifiedUserName = StringUtils.substringAfterLast(userName, "@");
                    if (tenantIdInQualifiedUserName.equals("")) {
                        throw new UserConfigurationException("Tenant Id cannot be empty or null in qualified user name.");
                    }

                    if (tenantIdInQualifiedUserName.equals(currentTenantId) || currentTenantId.equals(rootTenantId)) {
                        if (StringUtils.isEmpty((String) state[index])) { //check tenant id is null or empty
                            //(For non root tenants) set tenantId attribute in user entity to logged in users tenant id.
                            //(For root tenant) set tenantId attribute in user entity to tenantId in qualified user name which will
                            //be root tenantId when user is getting created in root tenant's space.
                            //and if user is getting created in other tenant set it to tenantId in qualified user name.
                            // tenantIdInQualifiedUserName is already verified for not null or empty.
                            state[index] = tenantIdInQualifiedUserName;
                            return true;
                        } else {
                            String tenantIdInUserEntity = state[index].toString();
                            if (tenantIdInUserEntity.equals(tenantIdInQualifiedUserName)) { //check tenantId attribute and tenantId in qualified username are same.
                                return false;
                            } else {
                                throw new UserConfigurationException("Tenant ID in qualified userName and tenantId attribute in entity should be same.");
                            }
                        }
                    } else {
                        //tenant (not root tenant) trying to create user in other tenant's space.
                        throw new CrossTenantOperationException("Access denied!");
                    }
                }
            }
            return false;
        } else {
            for (int index = 0; index < propertyName.length; index++) {
                if (propertyName[index].equals("tenantId")) {
                    String currentTenantId = AuthUtils.getCurrentTenantId();
                    if (StringUtils.isEmpty((String) state[index])) { //check tenant id is null or empty
                        state[index] = currentTenantId;  //Set tenantId attribute in entity with currentTenantId obtained from logged in user.
                        return true;
                    } else {
                        String entityTenantId = state[index].toString();
                        if (entityTenantId.equals(currentTenantId)) {
                            return false;
                        } else {
                            throw new CrossTenantOperationException("Access denied!");
                        }
                    }
                }
            }
            return false;
        }
    }

    private void checkCrossTenantOperation(Object entity, Object[] state, String[] propertyName, Serializable id) {
        if (entity instanceof Tenant) {
            return;
        } else {
            for (int index = 0; index < propertyName.length; index++) {
                if (propertyName[index].equals("tenantId")) {
                    String currentTenantId = AuthUtils.getCurrentTenantId();
                    if (state[index].toString().equals(currentTenantId)) {
                        return;
                    } else {
                        throw new CrossTenantOperationException("Access denied!");
                    }
                }
            }
        }
    }
}
