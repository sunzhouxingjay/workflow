/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.context.Context;

/**


 */
public class DeploymentEntityImpl extends AbstractEntityNoRevision implements DeploymentEntity, Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String category;
  protected String key;
  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected transient Map<String, ResourceEntity> resources;
  protected Date deploymentTime;
  protected transient boolean isNew;

  // Backwards compatibility
  protected String engineVersion;
  
  /**
   * Will only be used during actual deployment to pass deployed artifacts (eg process definitions). Will be null otherwise.
   */
  protected Map<Class<?>, List<Object>> deployedArtifacts;
  
  public DeploymentEntityImpl() {
    
  }

  @Override
  public void idGenerator() {
    this.id=name;
  }

  public void addResource(ResourceEntity resource) {
    if (resources == null) {
      resources = new HashMap<String, ResourceEntity>();
    }
    resources.put(resource.getName(), resource);
  }

  // lazy loading ///////////////////////////////////////////////////////////////
  public Map<String, ResourceEntity> getResources() {
    if (resources == null && id != null) {
      //sunzhouxing:自己加的先去查redis的resource,若没有则通过mybatis查询
      // List<ResourceEntity> resourcesList=Context.getCommandContext().getDbSqlSession().findResourcesByDeploymentIdFromRedis(id);
      // if (resourcesList.isEmpty()) {
      //   resourcesList = Context.getCommandContext().getResourceEntityManager().findResourcesByDeploymentId(id);
      // }
      List<ResourceEntity> resourcesList = Context.getCommandContext().getResourceEntityManager().findResourcesByDeploymentId(id);//原版
      resources = new HashMap<String, ResourceEntity>();
      for (ResourceEntity resource : resourcesList) {
        resources.put(resource.getName(), resource);
      }
    }
    return resources;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("category", this.category);
    persistentState.put("key", this.key);
    persistentState.put("tenantId", tenantId);
    return persistentState;
  }

  // Deployed artifacts manipulation ////////////////////////////////////////////
  
  public void addDeployedArtifact(Object deployedArtifact) {
    if (deployedArtifacts == null) {
      deployedArtifacts = new HashMap<Class<?>, List<Object>>();
    }

    Class<?> clazz = deployedArtifact.getClass();
    List<Object> artifacts = deployedArtifacts.get(clazz);
    if (artifacts == null) {
      artifacts = new ArrayList<Object>();
      deployedArtifacts.put(clazz, artifacts);
    }

    artifacts.add(deployedArtifact);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> getDeployedArtifacts(Class<T> clazz) {
    for (Class<?> deployedArtifactsClass : deployedArtifacts.keySet()) {
      if (clazz.isAssignableFrom(deployedArtifactsClass)) {
        return (List<T>) deployedArtifacts.get(deployedArtifactsClass);
      }
    }
    return null;
  }

  // getters and setters ////////////////////////////////////////////////////////

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
  
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public void setResources(Map<String, ResourceEntity> resources) {
    this.resources = resources;
  }

  public Date getDeploymentTime() {
    return deploymentTime;
  }

  public void setDeploymentTime(Date deploymentTime) {
    this.deploymentTime = deploymentTime;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  public String getEngineVersion() {
    return engineVersion;
  }

  public void setEngineVersion(String engineVersion) {
    this.engineVersion = engineVersion;
  }

  // common methods //////////////////////////////////////////////////////////

  @Override
  public String toString() {
    return "DeploymentEntity[id=" + id + ", name=" + name + "]";
  }

}