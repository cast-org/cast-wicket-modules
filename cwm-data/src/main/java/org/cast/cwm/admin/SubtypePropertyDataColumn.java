package org.cast.cwm.admin;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * DataColumn for a property that only exists in a subtype of the base type.
 * Works the same as @PropertyDataColumn, but will put blank cells in for any
 * data row that is not of the correct subtype.
 * 
 * @param <T> the row type of the data grid
 *  
 * @author bgoldowsky
 *
 */
public class SubtypePropertyDataColumn<T> extends PropertyDataColumn<T> {

	private static final long serialVersionUID = 1L;
	private Class<? extends T> subtype;
	
	public SubtypePropertyDataColumn(String headerString, String propertyExpression, Class<? extends T> subtype) {
		super(headerString, propertyExpression);
		this.subtype = subtype;
	}

	@Override
	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
		if (subtype.isInstance(rowModel.getObject()))
			super.populateItem(cellItem, componentId, rowModel);
		else
			cellItem.add(new EmptyPanel(componentId));
	}

	@Override
	public String getItemString(IModel<T> rowModel) {
		if (subtype.isInstance(rowModel.getObject()))
			return super.getItemString(rowModel);
		else
			return "";
	}
}