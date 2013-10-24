package org.cast.cwm.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.cast.cwm.data.User;
import org.cast.cwm.data.UserContent;
import org.cast.cwm.data.builders.UserContentAuditQueryBuilder;
import org.cast.cwm.data.provider.AuditDataProvider;
import org.cast.cwm.data.provider.AuditTriple;
import org.hibernate.envers.DefaultRevisionEntity;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@AuthorizeInstantiation("RESEARCHER")
public class UserContentLogPage extends AdminPage {

	private static final long serialVersionUID = 1L;
	
	protected static final int ITEMS_PER_PAGE = 50;

	protected static final String eventDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	
	public UserContentLogPage(PageParameters parameters) {
		super(parameters);

		AuditDataProvider<UserContent, DefaultRevisionEntity> provider = getDataProvider();
		DataTable<AuditTriple<UserContent,DefaultRevisionEntity>> table 
			= new DataTable<AuditTriple<UserContent,DefaultRevisionEntity>>("table", makeColumns(), provider, ITEMS_PER_PAGE);
		table.addTopToolbar(new HeadersToolbar(table, provider));
		table.addBottomToolbar(new NavigationToolbar(table));
		table.addBottomToolbar(new NoRecordsToolbar(table, new Model<String>("No revisions found")));
		add(table);
	}

	public AuditDataProvider<UserContent, DefaultRevisionEntity> getDataProvider() {
		UserContentAuditQueryBuilder qb = new UserContentAuditQueryBuilder();
		return new AuditDataProvider<UserContent,DefaultRevisionEntity>(qb);	
	}

	protected List<IColumn<AuditTriple<UserContent,DefaultRevisionEntity>>> makeColumns() {
		List<IColumn<AuditTriple<UserContent,DefaultRevisionEntity>>> columns = new ArrayList<IColumn<AuditTriple<UserContent,DefaultRevisionEntity>>>();
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("Rev ID"), "info.id"));
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("Rev Date"), "info.revisionDate") {
			private static final long serialVersionUID = 1L;
			protected IModel<String> createLabelModel(final IModel<AuditTriple<UserContent,DefaultRevisionEntity>> rowModel) {
				@SuppressWarnings("unchecked")
				IModel<Date> result = (IModel<Date>) super.createLabelModel(rowModel);
				DateTimeFormatter formatter = DateTimeFormat.forPattern(eventDateFormat);
				return Model.of(formatter.print(result.getObject().getTime()));				
			}
		});
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("Rev Type"), "type"));
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("UC ID"), "entity.id"));
		//columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("User"), "entity.user.subjectId"));
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("Type"), "entity.dataType"));
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("Sort Order"), "entity.sortOrder"));
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("Title"), "entity.title"));
		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("Text"), "entity.text"));

		columns.add(new PropertyColumn<AuditTriple<UserContent,DefaultRevisionEntity>>(Model.of("User"), "entity.user") {
			private static final long serialVersionUID = 1L;
			protected IModel<String> createLabelModel(final IModel<AuditTriple<UserContent,DefaultRevisionEntity>> rowModel) {
				@SuppressWarnings("unchecked")
				IModel<User> result = (IModel<User>) super.createLabelModel(rowModel);
				if (result.getObject() == null)
					return Model.of("null");
				else 
					return Model.of(result.getObject().getSubjectId());
			}
		});

		return columns;

	}
	
}
