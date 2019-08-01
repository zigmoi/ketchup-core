package org.zigmoi.ketchup.iam.configuration;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.common.AuthUtils;
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
        // return addTenantId(entity, currentState, propertyNames, id);
        checkCrossTenantOperation(entity, currentState, propertyNames, id);
        return false;
    }

    @Override //called before delete
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // addTenantId(entity, state, propertyNames, id);
        checkCrossTenantOperation(entity, state, propertyNames, id);
    }

    private boolean addTenantId(Object entity, Object[] state, String[] propertyName, Serializable id) {
        //Id can be accessed but cannot be modified, doesnt work even if its updated.
        if (entity instanceof User) {

            for (int index = 0; index < propertyName.length; index++) {
                if (propertyName[index].equals("tenantId")) {
                    System.out.println("In Interceptor tenantId");
                    String currentTenantId = AuthUtils.getCurrentTenantId();
                    String rootTenantId = "zigmoi.com";

                    //Allow rootTenant to create default user named 'admin' in other tenants.
                    if (currentTenantId.equals(rootTenantId)) {
                        //Check tenantId attribute is not null.
                        String tenantIdInUserEntity = "";
                        if (state[index] == null) {
                            throw new UserConfigurationException("Invalid Tenant Id, tenantId attribute is null.");
                        } else {
                            tenantIdInUserEntity = state[index].toString();
                        }

                        //Check tenantId in qualified username and tenantId attribute are same.
                        String tenantIdInQualifiedUserName = StringUtils.substringAfterLast(id.toString(), "@");
                        if (tenantIdInQualifiedUserName.equals(tenantIdInUserEntity)) {
                            return false;
                        } else {
                            throw new UserConfigurationException("Tenant ID in qualified userName attribute and tenantId attribute should be same.");
                        }
                    } else {
                        //Reject users where tenantId in qualified user name doesnt match currently logged in users tenantId.
                        String tenantIdInQualifiedUserName = StringUtils.substringAfterLast(id.toString(), "@");
                        if (tenantIdInQualifiedUserName.equals(currentTenantId)) {
                            //Set tenantId attribute in User entity with currentTenantId obtained from logged in user.
                            state[index] = currentTenantId;
                            return true;
                        } else {
                            throw new CrossTenantOperationException("Access denied!");
                        }
                    }
                }
            }
            //  throw new ClassCastException();
        }
        return false;
    }

    private void checkCrossTenantOperation(Object entity, Object[] state, String[] propertyName, Serializable id) {
        if (entity instanceof User) {
            for (int index = 0; index < propertyName.length; index++) {
                if (propertyName[index].equals("tenantId")) {
                    System.out.println("In Interceptor checkCrossTenantOperation");
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