package com.kuka.cr.opcua;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.server.UaServer;

/**
 * Represents a browse path in order to identify nodes in an OPC UA server's
 * information model.
 * <p>
 * This class represents browse paths as a sequence of [namespace, browse name]
 * pairs. No knowledge about the namespace index is required here, since the
 * namespace index is server specific and can only be determined when there is a
 * connection to the OPC UA server. This class, however, allows for specifying
 * browse paths prior to setting up a server connection.
 * 
 * @author Juergen Bock
 * @since 0.1.0
 * 
 */
public class OPCUABrowsePath {
	private static final Logger logger = LoggerFactory
			.getLogger(OPCUABrowsePath.class);
	private List<PathElement> elements;

	/**
	 * Creates a new instance.
	 * 
	 * @since 0.1.0
	 */
	public OPCUABrowsePath() {
		elements = new ArrayList<PathElement>();
	}

	/**
	 * Appends a new path element to the end of the browse path.
	 * 
	 * @param namespace
	 *            Namespace of the node.
	 * @param browseName
	 *            Browse name of the node.
	 * @throws NullPointerException
	 *             if browseName is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if namespace or browseName is empty.
	 * @since 0.1.0
	 */
	public void append(String namespace, String browseName) {
		if (logger.isDebugEnabled())
			logger.debug("Adding namespace=\"" + namespace
					+ "\", browse name=\"" + browseName
					+ "\" to the browse path");
		PathElement newElement = new PathElement(namespace, browseName);
		elements.add(newElement);
	}

	/**
	 * Renders the complete browse path as a string, converting the namespaces
	 * into namespace indices by looking up the namespace table on the server.
	 * 
	 * @param connector
	 *            Connector that maintains the connection to the OPC UA server
	 *            from which to obtain the namespace table.
	 * @return Browse path as string
	 * @since 0.1.0
	 */
	public String generateIndexedBrowsePath(UaServer connector) {
		if (logger.isInfoEnabled())
			logger.info("Generating namespace-indexed browse path for: "
					+ this.toString());

		StringBuilder sb = new StringBuilder();

		for (PathElement element : elements) {
			if (element.getNamespace() != null) {
				try {
					int namespaceIndex = connector.getNamespaceTable()
							.getIndex(element.getNamespace());
					sb.append(namespaceIndex);
					sb.append(":");
				} catch (NoSuchElementException e) {
					logger.warn("namespace \""
							+ element.getNamespace()
							+ "\"was not found in the namespace table on the OPC UA server.");
				}
			}
			sb.append(element.getBrowseName());
			sb.append("/");
		}

		if (logger.isDebugEnabled())
			logger.debug("Namespace-indexed browse path: \"" + sb.toString()
					+ "\"");

		return sb.toString();
	}

	/**
	 * Checks if this browse path is empty, i.e., if it has no path elements.
	 * 
	 * @return <code>true</code> if this browse path has no path elements,
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (PathElement e : elements)
			sb.append(e.toString());
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OPCUABrowsePath other = (OPCUABrowsePath) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}

	/**
	 * Represents an element in the browse path, which consists of a namespace
	 * URI and a browse name.
	 * 
	 * @author Juergen Bock
	 * @since 0.1.0
	 * 
	 */
	private static class PathElement {

		private final static Logger logger = LoggerFactory
				.getLogger(OPCUABrowsePath.PathElement.class);

		private final String namespace;
		private final String browseName;

		/**
		 * Creates a new path element. The namespace can be <code>null</code>.
		 * 
		 * @param namespace
		 *            Namespace.
		 * @param browseName
		 *            Browse path.
		 */
		public PathElement(String namespace, String browseName) {
			guard_PathElement(namespace, browseName);
			this.namespace = namespace;
			this.browseName = browseName;
		}

		/**
		 * Gets the namespace of this path element.
		 * 
		 * @return Namespace
		 */
		public String getNamespace() {
			return namespace;
		}

		/**
		 * Gets the browse name of this path element.
		 * 
		 * @return Browse name
		 */
		public String getBrowseName() {
			return browseName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return namespace + ":" + browseName + "/";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((browseName == null) ? 0 : browseName.hashCode());
			result = prime * result
					+ ((namespace == null) ? 0 : namespace.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PathElement other = (PathElement) obj;
			if (browseName == null) {
				if (other.browseName != null)
					return false;
			} else if (!browseName.equals(other.browseName))
				return false;
			if (namespace == null) {
				if (other.namespace != null)
					return false;
			} else if (!namespace.equals(other.namespace))
				return false;
			return true;
		}

		/**
		 * Guard for the constructor. Both arguments must not be empty, and
		 * browseName must not be <code>null</code>.
		 * 
		 * @param namespace
		 *            argument to check.
		 * @param browseName
		 *            argument to check.
		 * @throws NullPointerException
		 *             if browseName argument was <code>null</code>.
		 * @throws IllegalArgumentException
		 *             if any argument was empty.
		 */
		private void guard_PathElement(String namespace, String browseName) {
			if (namespace != null && namespace.isEmpty()) {
				final String msg = "namespace was empty.";
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
			if (browseName == null) {
				final String msg = "browseName was null.";
				logger.error(msg);
				throw new NullPointerException(msg);
			}
			if (browseName.isEmpty()) {
				final String msg = "browseName was empty.";
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}

	}

}
