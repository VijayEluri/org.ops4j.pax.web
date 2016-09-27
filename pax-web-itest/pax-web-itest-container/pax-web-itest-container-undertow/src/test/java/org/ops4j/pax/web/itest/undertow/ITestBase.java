/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.web.itest.undertow;

import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.web.itest.base.*;
import org.ops4j.pax.web.service.spi.ServletListener;
import org.ops4j.pax.web.service.spi.WebListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.XnioProvider;

import javax.inject.Inject;

import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.OptionUtils.combine;

@ExamReactorStrategy(PerMethod.class)
public class ITestBase {

	protected static final String WEB_CONTEXT_PATH = "Web-ContextPath";
	protected static final String WEB_CONNECTORS = "Web-Connectors";
	protected static final String WEB_VIRTUAL_HOSTS = "Web-VirtualHosts";
	protected static final String WEB_BUNDLE = "webbundle:";

	protected static final String COVERAGE_COMMAND = "coverage.command";


	protected static final String REALM_NAME = "realm.properties";

	private static final Logger LOG = LoggerFactory.getLogger(ITestBase.class);

	@Inject
	protected BundleContext bundleContext;

	@Inject
	protected XnioProvider xnioProvider;

	protected WebListener webListener;

	protected ServletListener servletListener;

	protected HttpTestClient testClient;

	public static Option[] baseConfigure() {
		new File("target/logs").mkdirs();
		return options(
				workingDirectory("target/paxexam/"),
				cleanCaches(true),
				junitBundles(),
				frameworkProperty("osgi.console").value("6666"),
				frameworkProperty("osgi.console.enable.builtin").value("true"),
				frameworkProperty("felix.bootdelegation.implicit").value(
						"false"),
				// frameworkProperty("felix.log.level").value("4"),
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("WARN"),
				systemProperty("org.osgi.service.http.hostname").value(
						"127.0.0.1"),
				systemProperty("org.osgi.service.http.port").value("8181"),
				systemProperty("java.protocol.handler.pkgs").value(
						"org.ops4j.pax.url"),
				systemProperty("org.ops4j.pax.url.war.importPaxLoggingPackages")
						.value("true"),
				systemProperty("org.ops4j.pax.web.log.ncsa.enabled").value(
						"true"),
				systemProperty("org.ops4j.pax.web.log.ncsa.directory").value(
						"target/logs"),
				systemProperty("org.ops4j.pax.web.jsp.scratch.dir").value("target/paxexam/scratch-dir"),
				systemProperty("ProjectVersion").value(
						VersionUtil.getProjectVersion()),
				systemProperty("org.ops4j.pax.url.mvn.certificateCheck").value("false"),

				addCodeCoverageOption(),

				mavenBundle().groupId("org.ops4j.pax.web.itest")
						.artifactId("pax-web-itest-base").versionAsInProject(),

				// do not include pax-logging-api, this is already provisioned
				// by Pax Exam
				mavenBundle().groupId("org.ops4j.pax.logging")
						.artifactId("pax-logging-log4j2")
						.version("1.8.5"),

				mavenBundle().groupId("org.ops4j.pax.logging")
						.artifactId("pax-logging-api")
						.version("1.8.5"),

				mavenBundle().groupId("org.ops4j.pax.url")
						.artifactId("pax-url-war").type("jar").classifier("uber").version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-spi").version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-api").version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-extender-war")
						.version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-extender-whiteboard")
						.version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-runtime").version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-jsp").version(asInProject()),
				mavenBundle().groupId("org.eclipse.jdt.core.compiler")
						.artifactId("ecj").version(asInProject()),

				mavenBundle().groupId("org.ops4j.pax.url")
						.artifactId("pax-url-aether").version(asInProject()).type("jar"),

				mavenBundle().groupId("org.apache.xbean")
						.artifactId("xbean-reflect").version(asInProject()),
				mavenBundle().groupId("org.apache.xbean")
						.artifactId("xbean-finder").version(asInProject()),
				mavenBundle().groupId("org.apache.xbean")
						.artifactId("xbean-bundleutils").version(asInProject()),
				mavenBundle().groupId("org.ow2.asm")
						.artifactId("asm-all").version(asInProject()),

				mavenBundle("commons-codec", "commons-codec").version(
						asInProject()),
				mavenBundle("org.apache.felix", "org.apache.felix.eventadmin")
						.version(asInProject()),
				wrappedBundle(mavenBundle("org.apache.httpcomponents",
						"httpcore").version(asInProject())),
				wrappedBundle(mavenBundle("org.apache.httpcomponents",
						"httpmime").version(asInProject())),
				wrappedBundle(mavenBundle("org.apache.httpcomponents",
						"httpclient").version(asInProject())));
	}

	public static Option[] configureBaseWithServlet() {
		return combine(
				baseConfigure(),
				mavenBundle().groupId("javax.servlet")
						.artifactId("javax.servlet-api").versionAsInProject());
	}

	public static Option[] configureUndertow() {
		return combine(
				configureBaseWithServlet(),
				mavenBundle("org.apache.felix", "org.apache.felix.scr")
						.versionAsInProject(),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-undertow").version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-runtime").version(asInProject()),

				mavenBundle().groupId("javax.annotation")
						.artifactId("javax.annotation-api").version(asInProject()),
				mavenBundle().groupId("org.jboss.logging")
						.artifactId("jboss-logging").version(asInProject()),
				mavenBundle()
						.groupId("org.ops4j.pax.tipi")
						.artifactId("org.ops4j.pax.tipi.xnio.api")
						.version(asInProject()),
				mavenBundle()
						.groupId("org.ops4j.pax.tipi")
						.artifactId("org.ops4j.pax.tipi.xnio.nio")
						.version(asInProject()),
				mavenBundle()
						.groupId("org.ops4j.pax.tipi")
						.artifactId("org.ops4j.pax.tipi.undertow.core")
						.version(asInProject()),
				mavenBundle()
						.groupId("org.ops4j.pax.tipi")
						.artifactId("org.ops4j.pax.tipi.undertow.servlet")
						.version(asInProject())
		);
	}

	public static Option[] configureUndertowBundle() {
		return combine(
				baseConfigure(),
				systemPackages("javax.xml.namespace;version=1.0.0", "javax.transaction;version=1.1.0"),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-undertow-bundle").version(asInProject())
		);
	}

	public static Option[] configureWebSocketUndertow() {
		return combine(
				configureUndertow(),
				mavenBundle().groupId("org.ops4j.pax.tipi")
						.artifactId("org.ops4j.pax.tipi.undertow.websocket-jsr").version(asInProject())
		);
	}


	@Before
	public void setUpITestBase() throws Exception {
		testClient = new HttpTestClient();
	}

	@After
	public void tearDownITestBase() throws Exception {
		testClient.close();
		testClient = null;
	}

	protected void initWebListener() {
		webListener = new WebListenerImpl();
		bundleContext.registerService(WebListener.class, webListener, null);
	}

	protected void initServletListener() {
		initServletListener(null);
	}

	protected void initServletListener(String servletName) {
		if (servletName == null)
			servletListener = new ServletListenerImpl();
		else
			servletListener = new ServletListenerImpl(servletName);
		bundleContext.registerService(ServletListener.class, servletListener,
				null);
	}

	protected void waitForWebListener() throws InterruptedException {
		new WaitCondition("webapp startup") {
			@Override
			protected boolean isFulfilled() {
				return ((WebListenerImpl) webListener).gotEvent();
			}
		}.waitForCondition();
	}

	protected void waitForServletListener() throws InterruptedException {
		new WaitCondition("servlet startup") {
			@Override
			protected boolean isFulfilled() {
				return ((ServletListenerImpl) servletListener).gotEvent();
			}
		}.waitForCondition();
	}

	protected void waitForServer(final String path) throws InterruptedException {
		new WaitCondition("server") {
			@Override
			protected boolean isFulfilled() throws Exception {
				return testClient.checkServer(path);
			}
		}.waitForCondition();
	}

	protected Bundle installAndStartBundle(String bundlePath)
			throws BundleException, InterruptedException {
		final Bundle bundle = bundleContext.installBundle(bundlePath);
		bundle.start();
		new WaitCondition("bundle startup") {
			@Override
			protected boolean isFulfilled() {
				return bundle.getState() == Bundle.ACTIVE;
			}
		}.waitForCondition();
		return bundle;
	}

	private static Option addCodeCoverageOption() {
		String coverageCommand = System.getProperty(COVERAGE_COMMAND);
		if (coverageCommand != null && coverageCommand.length() > 0) {
			LOG.info("found coverage option {}", coverageCommand);
			return CoreOptions.vmOption(coverageCommand);
		}
		return null;
	}

}
