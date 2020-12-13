package org.zigmoi.ketchup.application.services;

import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.models.V1Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeploymentResourceEventHandler implements ResourceEventHandler<V1Deployment> {

    @Autowired
    private ApplicationService applicationService;

    @Override
    public void onAdd(V1Deployment v1Deployment) {
        if(v1Deployment.getMetadata().getName().startsWith("app-")){
            System.out.println("Deployment added: " + v1Deployment.toString());
        }else{
            System.out.println("Deployment event ignored.");
        }
    }

    @Override
    public void onUpdate(V1Deployment v1Deployment, V1Deployment apiType1) {
        if(v1Deployment.getMetadata().getName().startsWith("app-")){
            System.out.println("Deployment updated: " + v1Deployment.toString());
        }else{
            System.out.println("Deployment event ignored.");
        }
    }

    @Override
    public void onDelete(V1Deployment v1Deployment, boolean b) {
        if(v1Deployment.getMetadata().getName().startsWith("app-")){
            System.out.println("Deployment deleted: " + v1Deployment.toString());
        }else{
            System.out.println("Deployment event ignored.");
        }
    }
}
