package org.zigmoi.ketchup.iam.configurations;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.util.ReflectionUtils;
import org.zigmoi.ketchup.exception.ConfigurationException;
import org.zigmoi.ketchup.iam.commons.AuthUtils;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.exceptions.CrossTenantOperationException;

import java.io.Serializable;
import java.lang.reflect.Field;

@Slf4j
public class TenantInterceptor extends EmptyInterceptor {

    @Override //called before insert
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        log.debug("Interceptor onSave");
        if (entity instanceof Tenant || entity instanceof User) {
            return false;
        } else {
            checkCrossTenantOperation(id);
        }
        return false;
    }

    @Override //called before update
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        log.debug("Interceptor onUpdate");
        if (entity instanceof Tenant || entity instanceof User) {
            return false;
        } else {
            checkCrossTenantOperation(id);
        }
        return false;
    }

    @Override //called before delete
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        log.debug("Interceptor onDelete");
        if (entity instanceof Tenant || entity instanceof User) {
            return;
        } else {
            checkCrossTenantOperation(id);
        }
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
            throws CallbackException {
        log.debug("Interceptor onLoad");
        if (entity instanceof Tenant || entity instanceof User) {
            return false;
        } else {
            checkCrossTenantOperation(id);
        }
        return false;
    }

    private void checkCrossTenantOperation(Serializable id) {
        try {
            Field field = ReflectionUtils.findField(id.getClass(), "tenantId");
            if (field == null) {
                throw new ConfigurationException("Entity does not have tenantId field in its primary key.");
            }
            ReflectionUtils.makeAccessible(field);
            Object fieldValue = field.get(id);
            if (fieldValue == null) {
                throw new ConfigurationException("Entity has invalid tenantId, null encountered.");
            }
            String requestedTenantId = fieldValue.toString();
            log.debug("Requested Tenant Id: " + requestedTenantId);
            String currentTenantId = AuthUtils.getCurrentTenantId();
            log.debug("Current Tenant Id: " + currentTenantId);
            if (requestedTenantId.equals(currentTenantId) == false) {
                throw new CrossTenantOperationException("Access denied!");
            }
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Entity does not have tenantId field in its primary key.", e);
        }
    }

}
