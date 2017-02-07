/*
 * Copyright 2011-2016 CAST, Inc.
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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Scopes;
import lombok.Getter;
import net.databinder.auth.hib.AuthDataApplication;
import net.databinder.components.hib.DataForm;
import net.databinder.hib.Databinder;
import net.databinder.hib.SessionUnit;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.file.File;
import org.cast.cwm.admin.*;
import org.cast.cwm.data.*;
import org.cast.cwm.data.init.CloseOldLoginSessions;
import org.cast.cwm.data.init.CreateAdminUser;
import org.cast.cwm.data.init.CreateDefaultUsers;
import org.cast.cwm.data.init.IDatabaseInitializer;
import org.cast.cwm.service.*;
import org.cwm.db.service.DBService;
import org.cwm.db.service.HibernateObjectModelProvider;
import org.cwm.db.service.IDBService;
import org.cwm.db.service.IModelProvider;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** 
 * An abstract Application class that CWM-based apps can extend.
 * Configures the application to use the User and CwmSession objects
 * that cwm-model defines, and attempts to load application properties 
 * at start time.
 * 
 * This class expects there to be a java initialization parameter named
 * "appConfig" (or, for back-compatibility, "propertiesFile"),
 * containing the absolute path to a file of application properties
 * (which can then be accessed from the application object via {@link #getConfiguration()}).
 * 
 * Application configuration dproperties that should be provided include:
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
public abstract class CwmApplication extends AuthDataApplication<User> {

	public static final String RESPTYPE_TEXT = "TEXT";
	public static final String RESPTYPE_HTML = "HTML";
	public static final String RESPTYPE_AUDIO = "AUDIO";
	public static final String RESPTYPE_SVG = "SVG";
	public static final String RESPTYPE_UPLOAD = "UPLOAD";
	public static final String RESPTYPE_TABLE = "TABLE";
	public static final String RESPTYPE_SINGLE_SELECT = "SINGLE_SELECT";
	public static final String RESPTYPE_SCORE = "SCORE";
	@Getter
	protected IAppConfiguration configuration;
	
	@Getter 
	protected String appInstanceId;

	@Getter 
	protected int sessionTimeout; // Session timeout, in seconds.  
	protected final int DEFAULT_SESSION_TIMEOUT = 90*60; // Defaults to 90 minutes
	
	@Getter 
	private String mailHost;
	
	@Getter
	private String mailFromAddress;
	
	@Inject
	private IEventService eventService;
	
	@Inject 
	private IResponseTypeRegistry responseTypeRegistry;

	@Inject
	private IAdminPageService adminPageService;
	
	private LoginSessionCloser loginSessionCloser;
	
	private static final Logger log = LoggerFactory.getLogger(CwmApplication.class);
		
    // A few things that need to get set up before regular init().
	@Override
	protected void internalInit() {
		log.debug("Starting CWM Application Internal Init");
		log.debug("Application Class is " + getClass().getName());
		
		loadAppProperties();

		// If using Logback as the logger, and we have a logConfig property,
		// then read that configuration.
		File logConfig = configuration.getOptionalFile("cwm.logConfig");
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
	    
	    loadServices();
	    
		getComponentInstantiationListeners().add(new GuiceComponentInjector(this, getInjectionModuleArray()));
		
		super.internalInit();
	}

	/**
	 * Load services needed before Guice setup.
	 * 
	 * To be overriden in subclasses...
	 * 
	 */
	protected void loadServices() {
	}

	@Override
	protected void init() {
		log.debug("Starting CWM Application Init");
		super.init();
		
	    Injector.get().inject(this);
		
		mailHost   = configuration.getProperty("cwm.mailHost");
		mailFromAddress = configuration.getProperty("cwm.mailFromAddress");
		
		getDebugSettings().setOutputMarkupContainerClassName(true);		

		initResponseTypes();
		loadContent();
		runDatabaseInitializers();
		configureMountPaths();
		
		loginSessionCloser = new LoginSessionCloser(this);
		loginSessionCloser.start();
		
		log.debug("Finished CWM Application Init");
	}

	private Module[] getInjectionModuleArray() {
		return getInjectionModules().toArray(new Module[0]);
	}
	
	/**
	 * Returns a list of default implementations of Guice Modules to be injected.
	 * Applications should override this method to inject services.
	 * 
	 * A typical usage should be to call super.getInjectionModules() to get the list, and then add to that list.
	 * 
	 * @return list of com.google.inject.Module objects
	 */
	protected List<Module> getInjectionModules() {
		ArrayList<Module> modules = new ArrayList<Module>();
		modules.add(new Module() {
			@Override
			public void configure(Binder binder) {
				log.debug("Binding CWM Services");
				binder.bind(IResponseTypeRegistry.class).to(ResponseTypeRegistry.class).in(Scopes.SINGLETON);
				binder.bind(ICwmService.class).to(CwmService.class).in(Scopes.SINGLETON);
				binder.bind(ICwmSessionService.class).to(CwmSessionService.class).in(Scopes.SINGLETON);
				binder.bind(ISiteService.class).to(SiteService.class).in(Scopes.SINGLETON);
				binder.bind(IAppConfiguration.class).toInstance(configuration);
				binder.bind(IModelProvider.class).to(HibernateObjectModelProvider.class);
				binder.bind(IAdminPageService.class).to(AdminPageService.class);
				binder.bind(IUserService.class).to(UserService.class);
				binder.bind(IDBService.class).to(DBService.class);
			}
		});
		return modules;
	}

	/**
	 * Connect to the application's content.  
	 * To be overridden as necessary by applications.
	 */
	protected void loadContent() {
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

		mountPage("admin", adminPageService.getAdminHomePage());
		mountPage("sitelist", adminPageService.getSiteListPage());
		mountPage("site", adminPageService.getSiteEditPage());
		mountPage("period", adminPageService.getPeriodEditPage());
		mountPage("userlist", adminPageService.getUserListPage());
		mountPage("edituser", adminPageService.getUserEditPage());
		mountPage("bulkupdate", adminPageService.getBulkUpdatePage());

		mountPage("stats", DatabaseStatisticsPage.class);
		mountPage("cache", CacheManagementPage.class);
		mountPage("eventlog", EventLog.class);
		mountPage("uclog", UserContentLogPage.class);
		mountPage("ucview", UserContentViewPage.class);
		
		mountPage("sessions", SessionListPage.class);
	}
	
	/** 
	 * Execute database initialization objects given by {@link #getDatabaseInitializers()}.
	 */
	private void runDatabaseInitializers () {
		new DatabaseInitializerRunner(configuration).run(getDatabaseInitializers());
	}

// TODO: can't override session store any more - how to get this functionality back?
//	@Override
//	protected ISessionStore newSessionStore() {
//		return new CwmSessionStore(this, new DiskPageStore());
//	}

	public static CwmApplication get() {
		return (CwmApplication) Application.get();
	}

	@Override
	public Class<User> getUserClass() {
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
			return adminPageService.getAdminHomePage();
		return getHomePage();
	}
	
	@Override
	protected void configureHibernate(Configuration c) {
		// We don't actually want the defaults that Databinder sets.
		// super.configureHibernate(ac);

		File configFile = configuration.getFile("cwm.hibernateConfig");
		if (configFile == null)
			throw new RuntimeException("Hibernate config file must be specified with cwm.hibernateConfig property.");

		c.configure(configFile);

		addCwmHibernateClasses(c);
	}

	public static void addCwmHibernateClasses(Configuration c) {
		c.addAnnotatedClass(org.cast.cwm.data.BinaryFileData.class);
		c.addAnnotatedClass(org.cast.cwm.data.Event.class);
		c.addAnnotatedClass(org.cast.cwm.data.Initialization.class);
		c.addAnnotatedClass(org.cast.cwm.data.LoginSession.class);
		c.addAnnotatedClass(org.cast.cwm.data.Period.class);
		c.addAnnotatedClass(org.cast.cwm.data.Site.class);
		c.addAnnotatedClass(org.cast.cwm.data.User.class);
		c.addAnnotatedClass(org.cast.cwm.data.UserContent.class);
		c.addAnnotatedClass(org.cast.cwm.data.UserPreferenceBoolean.class);
		c.addAnnotatedClass(org.cast.cwm.data.UserPreferenceString.class);
	}

	public void loadAppProperties() {
		configuration = AppConfiguration.loadFor(this);
		appInstanceId = configuration.getString("cwm.instanceId", "unknown");
		sessionTimeout = configuration.getInteger("cwm.sessionTimeout", DEFAULT_SESSION_TIMEOUT);
	}
	
	// Called to create a Wicket session
	// Also sets the timeout at the servlet level to the requested value
	@Override
	public Session newSession(Request request, Response response) {
		if (request instanceof ServletWebRequest) {
			((ServletWebRequest) request).getContainerRequest()
					.getSession().setMaxInactiveInterval(sessionTimeout);
		}
		return new CwmSession(request);
	}
	
	// Called when a session is ending
	@Override
	public void sessionUnbound(final String sessionId) {
		super.sessionUnbound(sessionId);
		log.debug("sessionUnbound called: {}", sessionId);
		loginSessionCloser.closeQueue.add(sessionId);
	}

	/**
	 * Set up some default response types used by many applications.
	 */
	protected void initResponseTypes() {
		/*
		 * Plain text is stored using {@link UserContent#setText(String)}.
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_TEXT, new ResponseType(RESPTYPE_TEXT, "Write"));

		/*
		 * Styled HTML text is stored using {@link UserContent#setText(String)}.
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_HTML, new ResponseType(RESPTYPE_HTML, "Write"));
		
		/*
		 * Binary audio data is stored using {@link UserContent#setPrimaryFile(BinaryFileData)}.
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_AUDIO, new ResponseType(RESPTYPE_AUDIO, "Record"));
		
		/*
		 * SVG markup is stored using {@link UserContent#setText(String)}
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_SVG, new ResponseType(RESPTYPE_SVG, "Draw"));
		
		/*
		 * Uploaded files are stored using {@link UserContent#setPrimaryFile(BinaryFileData)}.
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_UPLOAD, new ResponseType(RESPTYPE_UPLOAD, "Upload"));
		
		/*
		 * Table markup is stored using {@link ResponseData#setText(String)}
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_TABLE, new ResponseType(RESPTYPE_TABLE, "Table"));

		/*
		 * A response to a single-select, multiple choice prompt.  Actual answer stored using {@link UserContent#setText(String)}.
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_SINGLE_SELECT, new ResponseType(RESPTYPE_SINGLE_SELECT, "Multiple Choice"));
		
		/*
		 * A score of some type -- star rating, rubric value, etc.
		 */
		responseTypeRegistry.registerResponseType(RESPTYPE_SCORE, new ResponseType(RESPTYPE_SCORE, "Score"));
		
	}
	
	public IResponseType getResponseType(String name) {
		return responseTypeRegistry.getResponseType(name);
	}
	
	public Collection<IResponseType> getLegalResposeTypes() {
		return responseTypeRegistry.getLegalResponseTypes();
	}
	
	@Override
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
	
	@Override
	protected void onDestroy() {
		log.debug("Running shutdown steps");
		if (loginSessionCloser != null)
			loginSessionCloser.interrupt();
		this.getHibernateSessionFactory(null).close();
		super.onDestroy();
	}
	
	/**
	 * A separate thread that is deals with the asynchronous need to close LoginSessions
	 * that have timed out.  Connected to the application so that it can access service classes
	 * (the SessionStore can not).
	 */
	protected class LoginSessionCloser extends Thread {
		
		private BlockingQueue<String> closeQueue = new LinkedBlockingQueue<String>();
		private Application application;
		
		protected LoginSessionCloser (Application app) {
			super("LoginSessionCloser");
			this.application = app;
			this.setDaemon(true);
		}
		
		@Override
		public void run() {
			ThreadContext.setApplication(application);
			log.debug("LoginSessionCloser thread {} starting with application: {}", this, Application.get());

			while(true) {
				try {
					final String loginSessionId = closeQueue.take();
					Databinder.ensureSession(new SessionUnit() {
						@Override
						public Object run(org.hibernate.Session dbSession) {
							LoginSession loginSession = eventService.getLoginSessionBySessionId(loginSessionId).getObject();
							if (loginSession != null) {
								 if (loginSession.getEndTime() == null) {
									 log.debug("Closing login session {}", loginSessionId);
									 eventService.forceCloseLoginSession(loginSession, "[timed out]");
								 } else {
									 // If user logged out normally, login session would already be closed.
									 log.debug("Login session {} was already closed", loginSessionId);
								 }
							} else {
								// This is probably a web session where the user never logged in.
								log.debug("No LoginSession corresponds to session ID {}", loginSessionId);
							}
							dbSession.getTransaction().commit();
							return null;
						}						
					});
				} catch (InterruptedException e) {
					log.debug("LoginSessionCloser exiting due to interrupt");
					break;
				} catch (Exception e) {
					// Catch all other exceptions so that they don't terminate the thread
					log.error("Unexpected exception during login session closing (ignored):");
					e.printStackTrace();
				}
			}

		}
	}
	
}
