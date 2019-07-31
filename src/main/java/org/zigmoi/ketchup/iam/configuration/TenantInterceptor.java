package org.zigmoi.ketchup.iam.configuration;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.zigmoi.ketchup.iam.entities.TenantEntity;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.iam.exceptions.CrossTenantOperationException;

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
                    state[index] = currentTenantId; //set tenantId
                    return true;
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