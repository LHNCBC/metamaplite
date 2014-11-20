
//
package gov.nih.nlm.nls.metamap.lite.pipeline.plugins;

import java.lang.reflect.Method;

/**
 *
 */

public class Plugin 
{
  String name;
  String description;
  Object classInstance;
  Method method;

  public Plugin(String name, String description, Object classInstance, Method method) {
    this.name = name;
    this.description = description;
    this.classInstance = classInstance;
    this.method = method;
  }
  public String getName() {
    return this.name;
  }
  public String getDescription() {
    return this.description;
  }
  public Object getClassInstance() {
    return this.classInstance;
  }
  public Method getMethod() {
    return this.method;
  }
}
