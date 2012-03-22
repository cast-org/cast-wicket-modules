package org.cast.cwm.service;

import lombok.Getter;
import lombok.Setter;
import net.databinder.models.hib.HibernateObjectModel;

import org.apache.wicket.Application;
import org.cast.cwm.data.Period;
import org.cast.cwm.data.builders.PeriodCriteriaBuilder;

public class PeriodService {

	protected static PeriodService instance = new PeriodService();
	
	@Getter @Setter
	protected Class<? extends Period> periodClass = Period.class;

	protected PeriodService() { /* Protected Constructor - use PeriodService.get() */};
	
	public static PeriodService get() {
		return (PeriodService)instance;
	}

	/**
	 * Use this Service class.  Called in {@link Application#init()}.
	 */
	public static void useAsServiceInstance() {
		PeriodService.instance = new PeriodService();
	}

	
	/**
	 * Get the period by name - this assumes the name is unique
	 */
	public HibernateObjectModel<Period> getPeriodByName(String name) {
		PeriodCriteriaBuilder pcb = new PeriodCriteriaBuilder();
		pcb.setName(name);
		pcb.setMaxResults(1);
		return new HibernateObjectModel<Period>(periodClass, pcb);
	}
}