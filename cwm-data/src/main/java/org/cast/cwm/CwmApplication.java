/*
 * Copyright 2011 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import net.databinder.auth.data.DataUser;
import net.databinder.auth.hib.AuthDataApplication;
import net.databinder.hib.DataRequestCycle;
import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.pagestore.DiskPageStore;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.file.File;
import org.cast.cwm.admin.AdminHome;
import org.cast.cwm.admin.DatabaseStatisticsPage;
import org.cast.cwm.admin.EventLog;
import org.cast.cwm.admin.PeriodInfoPage;
import org.cast.cwm.admin.SessionListPage;
import org.cast.cwm.admin.SiteInfoPage;
import org.cast.cwm.admin.SiteListPage;
import org.cast.cwm.admin.UserFormPage;
import org.cast.cwm.admin.UserListPage;
import org.cast.cwm.data.IResponseType;
import org.cast.cwm.data.ResponseType;
import org.cast.cwm.data.Role;
import org.cast.cwm.data.init.CloseOldLoginSessions;
import org.cast.cwm.data.init.CreateAdminUser;
import org.cast.cwm.data.init.CreateDefaultUsers;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.cast.cwm.data.resource.SvgImageResource;
import org.cast.cwm.data.resource.UploadedFileResource;
import org.cast.cwm.service.CwmService;
import org.cast.cwm.service.EventService;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/** 
 * An abstract Application class that CWM-based apps can extend.
 * Configures the application to use the User and CwmSession objects
 * that cwm-model defines, and attempts to load application properties 
 * at start time.
 * 
 * This class expects there to be a java initialization parameter named
 * propertiesFile, containing the absolute path to a file of application
 * properties (which can then be accessed from the application object
 * via {@link #getAppProperties()}).
 * 
 * Application properties that should be provided include:
 *  * cwm.hibernateConfig: path to a configuration file for Hibernate
 *  * cwm.logConfig: path to a configuration file for Logback logging system.
 *  * cwm.instanceId: identifier for the particular server instance
 *  * cwm.sessionTimeout: number of seconds of inactivity before a user's 
 *    session will expire (optional; default = 90 minutes).
 *  
 *  Also see database initializers in {@link org.cast.cwm.data.init} package
 *  which use additional properties.
 * 
 * @author bgoldowsky
 */
public abstract class CwmApplication extends AuthDataApplication {
	
	@Getter
	protected Properties appProperties;
	
	@Getter 
	protected String appInstanceId;

	@Getter 
	protected int sessionTimeout = 90*60; // Session timeout, in seconds.  Defaults to 90 minutes
	
	@Getter 
	private String mailHost;
	
	@Getter
	private String mailFromAddress;
	
	private static final Logger log = LoggerFactory.getLogger(CwmApplication.class);
		
    // A few things that need to get set up before regular init().
	@Override
	protected void internalInit() {

		EventService.setInstance(new EventService());

		if(appProperties == null) {
			loadAppProperties();		
		}

		// If using Logback as the logger, and we have a logConfig property,
		// then read that configuration.
		String logConfig = appProperties.getProperty("cwm.logConfig");
	    if (logConfig != null
	    		&& LoggerFactory.getILoggerFactory() instanceof LoggerContext) { 
			log.info("Log Configuration: {}", logConfig);
	    	LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

	    	try {
	    		JoranConfigurator configurator = new JoranConfigurator();
	    		configurator.setContext(lc);
	    		// the context was probably already configured by default configuration rules
	    		lc.reset(); 
	    		configurator.doConfigure(logConfig);
	    	} catch (JoranException je) {
	    		je.printStackTrace();
	    	}
	    	StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
	    }

	    super.internalInit();
	}

	@Override
	protected void init() {
		super.init();
		
		mailHost   = appProperties.getProperty("cwm.mailHost");
		mailFromAddress = appProperties.getProperty("cwm.mailFromAddress");
		
		initResponseTypes();
		runDatabaseInitializers();
		configureMountPaths();
		
		// Mount Resource Handlers
		UploadedFileResource.mount(this);
		SvgImageResource.mount(this);
	}
	
	/**
	 * Returns the list of database initialization methods that will be run at startup.
	 * Applications should override this method if they want to define additional
	 * initialization steps.
	 * 
	 * @return list of initializer objects, which all must implement IDatabaseInitializer
	 */
	protected List<IDatabaseInitializer> getDatabaseInitializers() {
		LinkedList<IDatabaseInitializer> list = new LinkedList<IDatabaseInitializer>();
		list.add(new CreateAdminUser());
		list.add(new CreateDefaultUsers());
		list.add(new CloseOldLoginSessions());
		return list;
	}
	
	protected void configureMountPaths() {
		
		mountBookmarkablePage("admin", AdminHome.class);
		mountBookmarkablePage("stats", DatabaseStatisticsPage.class);
		mountBookmarkablePage("eventlog", EventLog.class);
		mountBookmarkablePage("sitelist", SiteListPage.class);
		mountBookmarkablePage("userlist", UserListPage.class);
		
		mount(new QueryStringUrlCodingStrategy("period", PeriodInfoPage.class));
		mount(new QueryStringUrlCodingStrategy("sessions", SessionListPage.class));
		mount(new QueryStringUrlCodingStrategy("site", SiteInfoPage.class));
		mount(new QueryStringUrlCodingStrategy("edituser", UserFormPage.class));
	}
	
	/** Execute database initialization objects given by {@link #getDatabaseInitializers()}.
	 */
	protected void runDatabaseInitializers () {
		Databinder.ensureSession(new SessionUnit() {
			public Object run(org.hibernate.Session session) {
				CwmService cwmService = CwmService.get();
				List<String> initsDone = cwmService.getInitializationNames();
				for (IDatabaseInitializer init : getDatabaseInitializers()) {
					// Run the initializer unless it's a one-time-only that's already done.
					if (!init.isOneTimeOnly() || !initsDone.contains(init.getName())) {
						log.debug("Running {}", init.getName());
						if (init.run(appProperties)) {
							// record the run if it did any work.
							cwmService.saveInitialization(init);
						}
					}
				}
				return null;
			}
		});
	}

	@Override
	protected ISessionStore newSessionStore() {
		return new CwmSessionStore(this, new DiskPageStore());
	}

	/** Used in testing, when you don't have the servlet context
	 * from which to pull the application properties path. If
	 * the app properties are set, then they will not be reloaded
	 * in the call to configureHibernate
	 * 
	 * @param appProperties the application configuration properties 
	 */
	public void setApplicationProperties(Properties appProperties) {
		this.appProperties = appProperties;
	}
	
	public static CwmApplication get() {
		return (CwmApplication) Application.get();
	}

	public Class<? extends DataUser> getUserClass() {
		return org.cast.cwm.data.User.class;
	}
	
	/**
	 * Return the home page for a particular user Role.
	 * This default implementation just returns the application's generic home page;
	 * applications should extend this if they wish to make use of role-specific home pages.
	 * @param role The user's role
	 * @return the page class of the home page.
	 */
	public Class<? extends Page> getHomePage(Role role) {
		if (role == Role.ADMIN)
			return AdminHome.class;
		return getHomePage();
	}
	
	@Override
	protected void configureHibernate(AnnotationConfiguration ac) {
		super.configureHibernate(ac);
		Configuration c = ac;
		
		String configFile = appProperties.getProperty("cwm.hibernateConfig");
		if (configFile == null)
			throw new RuntimeException ("Hibernate config file must be specified with cwm.hibernateConfig property.");
		
		c.configure(new File(configFile));

		c.addAnnotatedClass(org.cast.cwm.data.Event.class);
		c.addAnnotatedClass(org.cast.cwm.data.BinaryFileData.class);
		c.addAnnotatedClass(org.cast.cwm.data.LoginSession.class);
		c.addAnnotatedClass(org.cast.cwm.data.User.class);
		c.addAnnotatedClass(org.cast.cwm.data.Prompt.class);
		c.addAnnotatedClass(org.cast.cwm.data.Response.class);
		c.addAnnotatedClass(org.cast.cwm.data.ResponseData.class);		
		c.addAnnotatedClass(org.cast.cwm.data.Site.class);
		c.addAnnotatedClass(org.cast.cwm.data.Period.class);
		c.addAnnotatedClass(org.cast.cwm.data.Initialization.class);
	}
	
	public void loadAppProperties() {
		appProperties = new Properties();
		String propPath = getServletContext().getInitParameter("propertiesFile");
		if(propPath == null)
			throw new RuntimeException("No configuration properties file path set");
		log.info("Loading App Properties from {}", propPath);
		try {
			appProperties.load(new FileInputStream(propPath));
		} catch(Exception e) {
			throw new RuntimeException("Error configuring application", e);
		}
		appInstanceId = appProperties.getProperty("cwm.instanceId");
		
		// Look up sessionTimeout value
		String stString = appProperties.getProperty("cwm.sessionTimeout", "0");
		try {
			int st = Integer.valueOf(stString);
			if (st > 0)
				sessionTimeout = st;
			else if (st == 0)
				log.debug("SessionTimeout not specified, defaulting to {}", sessionTimeout);
			else {
				log.warn("SesstionTimeout invalid: {}", stString);
			}
		} catch (NumberFormatException e) {
			log.error("SessionTimeout invalid, must be an integer number of seconds: {}", stString);
		}
	}
	
	@Override
	public Session newSession(Request request, Response response) {
		return new CwmSession(request);
	}
	
	@Override
	public RequestCycle newRequestCycle (final Request request, final Response response) {
		return new DataRequestCycle (this, (WebRequest) request, (WebResponse) response) {
			
			@Override
			public Page onRuntimeException(final Page cause, final RuntimeException e) {
				super.onRuntimeException(cause, e);  // Executes some methods
				return CwmApplication.get().getExceptionPage(e);
			}
		};
	}
	
	
	// TODO: move these to a separate class, injected via Guice
	private Map<String,IResponseType> legalResponseTypes = new HashMap<String,IResponseType>();
	
	void initResponseTypes() {
		/**
		 * Plain text is stored using {@link ResponseData#ResponseData.setText(String)}.
		 */
		legalResponseTypes.put("TEXT", new ResponseType("TEXT", "Write")); 

		/**
		 * Styled HTML text is stored using {@link ResponseData#setText(String)}.
		 */
		legalResponseTypes.put("HTML", new ResponseType("HTML", "Write")); 
		
		/**
		 * Binary audio data is stored using {@link ResponseData#setBinaryFileData(BinaryFileData)}.
		 */
		legalResponseTypes.put("AUDIO", new ResponseType("AUDIO", "Record")); 
		
		/**
		 * SVG markup is stored using {@link ResponseData#setText(String)}
		 */
		legalResponseTypes.put("SVG", new ResponseType("SVG", "Draw"));
		
		/**
		 * Binary data is stored using {@link ResponseData#setBinaryFileData(BinaryFileData)}.
		 */
		legalResponseTypes.put("UPLOAD", new ResponseType("UPLOAD", "Upload"));
		
		/**
		 * Highlight colors and word indexes are stored as CSV using {@link ResponseData#ResponseData.setText(String)}.  
		 * For example: "R:1,2,3,5,6,7#Y:22,23,25,26"
		 */
		legalResponseTypes.put("HIGHLIGHT", new ResponseType("HIGHLIGHT", "Highlight"));
		
		/**
		 * A response to a cloze-type passage (fill in the missing words).  The actual answers
		 * are stored as CSV using {@link ResponseData#ResponseData.setText(String)}.
		 */
		legalResponseTypes.put("CLOZE", new ResponseType("CLOZE", "Cloze Passage")); 
		
		/**
		 * A response to a single-select, multiple choice prompt.  Actual answer stored using {@link ResponseData#setText(String)}.
		 */
		legalResponseTypes.put("SINGLE_SELECT", new ResponseType("SINGLE_SELECT", "Multiple Choice"));
		
		/**
		 * A rating (e.g. 1-5).  The value is stored using {@link ResponseData#setScore(int)}
		 */
		legalResponseTypes.put("STAR_RATING", new ResponseType("STAR_RATING", "Rate"));
		
		/**
		 * A generic score.  
		 * 
		 * TODO: Perhaps this can be used to replace Star Rating and combine Cloze/SingleSelect?
		 */
		legalResponseTypes.put("SCORE", new ResponseType("SCORE", "Score"));
		
		/**
		 * Applet markup is stored using {@link ResponseData#setText(String)}
		 */
		legalResponseTypes.put("APPLET", new ResponseType("APPLET", "applet"));
	}
	
	public IResponseType getResponseType(String name) {
		return legalResponseTypes.get(name);
	}
	
	public Collection<IResponseType> getLegalResposeTypes() {
		return legalResponseTypes.values();
	}
	
	
//	public Component getReponseViewer (org.cast.cwm.data.Response r) {
//		if (r.getType().equals(.r..r.))
//			return ResponseViewer.class;
//	}
	
	
	/**
	 * Override this method to display a custom page when an exception is thrown.
	 * @param e
	 * @return
	 */
	protected Page getExceptionPage(final RuntimeException e) {
		return null;
	}

	public byte[] getSalt() {
		return "mmmm salt, makes the encryption tasty".getBytes();
	}

	public String getAppAndInstanceId () {
		return getAppId() + "/" + appInstanceId;
	}
	
	public abstract String getAppId();
	
	@Override
	public Class<? extends WebPage> getSignInPageClass() {
		// TODO: Create a base login page
		throw new IllegalStateException("Way too many things go wrong without Cast's custom login.");
	}
}
