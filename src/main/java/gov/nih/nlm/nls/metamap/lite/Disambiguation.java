
package gov.nih.nlm.nls.metamap.lite;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import gov.nih.nlm.nls.wsd.algorithms.AEC.AECMethod;
import gov.nih.nlm.nls.wsd.algorithms.MRD.CandidateCUI;

import gov.nih.nlm.nls.metamap.lite.types.ConceptInfo;
import gov.nih.nlm.nls.metamap.lite.types.Entity;
import gov.nih.nlm.nls.metamap.lite.types.Ev;

/**
 * Disambiguation - a bridge to access Antonio Jimeno-Yepes AEC/MRD
 * Disambiguation method library.
 *
 * This could be modified to access various disambiguation methods.
 *
 * Created: Tue Sep 26 14:21:34 2017
 *
 * @author <a href="mailto:wjrogers@mail.nih.gov">Willie Rogers</a>
 * @version 1.0
 */
public class Disambiguation {

  AECMethod disambiguationMethod = new AECMethod();

  /**
   * Creates a new <code>Disambiguation</code> instance.
   *
   */
  public Disambiguation() {

  }

  public List<Entity> disambiguateEntityList(List<Entity> entityList, String text) {
    List<Entity> filteredEntityList = new ArrayList<Entity>();
    for (Entity entity: entityList) {
      List<CandidateCUI> cuis = new ArrayList<CandidateCUI>();
      Map<String,Ev> prefnameEvMap = new HashMap<String,Ev>();
      for (Ev ev: entity.getEvList()) {
	prefnameEvMap.put(ev.getConceptInfo().getPreferredName(), ev);
	cuis.add(new CandidateCUI(ev.getConceptInfo().getPreferredName(),
				  ev.getConceptInfo().getCUI()));
      }
      List<String> filteredPrefNames = this.disambiguationMethod.disambiguate(cuis, text);
      Set<Ev> newEvSet = new HashSet<Ev>();
      for (String prefName: filteredPrefNames) {
	if (prefnameEvMap.containsKey(prefName)) {
	  newEvSet.add(new Ev(prefnameEvMap.get(prefName)));
	}
      }
      Entity newEntity = new Entity(entity);
      entity.setEvSet(newEvSet);
      filteredEntityList.add(entity);
    }
    return filteredEntityList;
  }

}
