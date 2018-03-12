package com.kuka.cr.opcua;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.UserTokenPolicy;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.utils.EndpointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.PkiFileBasedCertificateValidator;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;
import com.prosysopc.ua.types.opcua.server.BuildInfoTypeNode;

/**
 * OPC UA Server initialization and general handling
 * 
 * @author Manuel Kaspar
 * @author Pierre Venet
 * 
 */
public class BasicOpcUaServer extends UaServer {
	private static Logger log = LoggerFactory.getLogger(BasicOpcUaServer.class.getName());

	final PkiFileBasedCertificateValidator validator = new PkiFileBasedCertificateValidator();

	/**
	 * 
	 * */
	public BasicOpcUaServer(int opcPort, int httpPort, String serverName, String applicationName) throws Exception {
		// TCP Port number for the UA Binary protocol
		this.setPort(Protocol.OpcTcp, opcPort);
		log.info("Server listening on " + "opc.tcp://localhost" + ":" + opcPort);

		// Optional server name part of the URI (default for all protocols)
		this.setServerName("OPCUA/" + serverName);

		/*
		 * Optionally restrict the InetAddresses to which the server is bound. You may
		 * also specify the addresses for each Protocol. This is the default
		 * (isEnableIPv6 defines whether IPv6 address should be included in the bound
		 * addresses. Note that it requires Java 7 or later to work in practice in
		 * Windows):
		 */
		this.setBindAddresses(EndpointUtil.getInetAddresses(isEnableIPv6()));
		this.setDiscoveryServerRegistrationPeriod(1);
		
		ApplicationDescription appDescription = new ApplicationDescription();
		appDescription.setApplicationName(new LocalizedText(applicationName, Locale.ENGLISH));
		appDescription.setApplicationUri("urn:localhost:OPCUA:" + applicationName);
		appDescription.setProductUri("urn:kuka.com:OPCUA:" + applicationName);

		try {
			this.setCertificates(appDescription);
		} catch (IOException | SecureIdentityException e) {
			log.error("Could not set the application:" + e.getMessage());
		}

		this.setSecuritySettings();

		try {
			this.init();
		} catch (UaServerException e) {
			log.error("Could not initialize the server: " + e.getMessage());
		}

		this.initBuildInfo();

		// "Safety limits" for ill-behaving clients
		this.getSessionManager().setMaxSessionCount(500);
		this.getSessionManager().setMaxSessionTimeout(3600000); // one hour
		this.getSubscriptionManager().setMaxSubscriptionCount(50);
	}

	private void setSecuritySettings() throws UaServerException {
		this.setSecurityModes(SecurityMode.ALL);

		// Define the supported user Token policies
		this.addUserTokenPolicy(UserTokenPolicy.ANONYMOUS);
		this.addUserTokenPolicy(UserTokenPolicy.SECURE_USERNAME_PASSWORD);
		this.addUserTokenPolicy(UserTokenPolicy.SECURE_CERTIFICATE_BASIC256);
	}

	private void setCertificates(ApplicationDescription appDescription) throws IOException, SecureIdentityException {
		File privatePath = new File(validator.getBaseDir(), "private");
		int[] keySizes = null;
		final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription,
				"KUKA Deutschland GmbH", "opcua", privatePath, null, keySizes, true);

		this.setApplicationIdentity(identity);
	}

	/**
	 * Stops the OPC-UA Server.
	 */
	public void stop() {
		log.info("Shutting down OPC UA Server...");
		this.shutdown(0, new LocalizedText("Closed by user", Locale.ENGLISH));
		log.info("Closed.");
	}

	/**
	 * Initialize the information to the Server BuildInfo structure
	 */
	private void initBuildInfo() {
		// Initialize BuildInfo - using the version info from the SDK
		// You should replace this with your own build information
		final BuildInfoTypeNode buildInfo = getNodeManagerRoot().getServerData().getServerStatusNode()
				.getBuildInfoNode();

		// Fetch version information from the package manifest
		final Package sdkPackage = UaServer.class.getPackage();
		final String implementationVersion = sdkPackage.getImplementationVersion();

		if (implementationVersion != null) {
			int splitIndex = implementationVersion.lastIndexOf(".");
			final String softwareVersion = implementationVersion.substring(0, splitIndex);
			String buildNumber = implementationVersion.substring(splitIndex + 1);

			buildInfo.setManufacturerName(sdkPackage.getImplementationVendor());
			buildInfo.setSoftwareVersion(softwareVersion);
			buildInfo.setBuildNumber(buildNumber);
		}
	}
}
