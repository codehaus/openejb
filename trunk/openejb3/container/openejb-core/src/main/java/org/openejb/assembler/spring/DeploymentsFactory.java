/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.assembler.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.openejb.DeploymentInfo;
import org.openejb.alt.config.DeployedJar;
import org.openejb.alt.config.DeploymentLoader;
import org.openejb.alt.config.EjbJarInfoBuilder;
import org.openejb.alt.config.ejb.EjbDeployment;
import org.openejb.assembler.classic.EjbJarBuilder;
import org.openejb.assembler.classic.EjbJarInfo;
import org.openejb.core.CoreDeploymentInfo;
import org.springframework.beans.factory.FactoryBean;

/**
 * @org.apache.xbean.XBean element="deployments"
 */
public class DeploymentsFactory implements FactoryBean {

    private AssemblyInfo assembly;
    private TransactionManager transactionManager;
    private Object value;
    private DeploymentLoader.Type type;

    public AssemblyInfo getAssembly() {
        return assembly;
    }

    public void setAssembly(AssemblyInfo assembly) {
        this.assembly = assembly;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public String getJar() {
        return (String) value;
    }

    public void setJar(String jar) {
        this.type = DeploymentLoader.Type.JAR;
        this.value = jar;
    }

    public String getDir() {
        return (String) value;
    }

    public void setDir(String dir) {
        this.type = DeploymentLoader.Type.DIR;
        this.value = dir;
    }

    public ClassLoader getClasspath() {
        return (ClassLoader) value;
    }

    public void setClasspath(ClassLoader classpath) {
        this.type = DeploymentLoader.Type.CLASSPATH;
        this.value = classpath;
    }

    // Singletons don't work
    private HashMap<String, DeploymentInfo> deployments;
    public Object getObject() throws Exception {
        if (deployments != null){
            return deployments;
        }
        HashMap context = new HashMap();
        context.put(TransactionManager.class.getName(), transactionManager);
        org.openejb.assembler.classic.Assembler.setContext(context);

        DeploymentLoader loader = new DeploymentLoader();
        List<DeployedJar> deployedJars = loader.load(type, value);

        EjbJarInfoBuilder infoBuilder = new EjbJarInfoBuilder();

        ClassLoader classLoader = (value instanceof ClassLoader) ? (ClassLoader) value : Thread.currentThread().getContextClassLoader();
        EjbJarBuilder builder = new EjbJarBuilder(classLoader);

        System.out.println("DeploymentsFactory.getObject");

        deployments = new HashMap();
        for (DeployedJar jar : deployedJars) {
            EjbJarInfo jarInfo = infoBuilder.buildInfo(jar);
            if (jarInfo == null){
                // This means the jar failed validation or otherwise could not be deployed
                // a message was already logged to the appropriate place.
                continue;
            }

            transferMethodTransactionInfos(infoBuilder);
            transferMethodPermissionInfos(infoBuilder);

            HashMap<String, DeploymentInfo> ejbs = builder.build(jarInfo);

            for (EjbDeployment data : jar.getOpenejbJar().getEjbDeployment()) {
                ((CoreDeploymentInfo)ejbs.get(data.getDeploymentId())).setContainer(new ContainerPointer(data.getContainerId()));
            }

            deployments.putAll(ejbs);
        }

        return deployments;
    }

    private void transferMethodTransactionInfos(EjbJarInfoBuilder infoBuilder) {
        List<MethodTransactionInfo> infos = new ArrayList();
        if (assembly.getMethodTransactions() != null){
            infos.addAll(Arrays.asList(assembly.getMethodTransactions()));
        }
        for (org.openejb.assembler.classic.MethodTransactionInfo info : infoBuilder.getMethodTransactionInfos()) {
            infos.add(new MethodTransactionInfo(info));
        }
        assembly.setMethodTransactions(infos.toArray(new MethodTransactionInfo[]{}));
    }

    private void transferMethodPermissionInfos(EjbJarInfoBuilder infoBuilder) {
        List<MethodPermissionInfo> infos = new ArrayList();
        if (assembly.getMethodPermissions() != null){
            infos.addAll(Arrays.asList(assembly.getMethodPermissions()));
        }
        for (org.openejb.assembler.classic.MethodPermissionInfo info : infoBuilder.getMethodPermissionInfos()) {
            infos.add(new MethodPermissionInfo(info));
        }
        assembly.setMethodPermissions(infos.toArray(new MethodPermissionInfo[]{}));
    }

    public Class getObjectType() {
        return Map.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
