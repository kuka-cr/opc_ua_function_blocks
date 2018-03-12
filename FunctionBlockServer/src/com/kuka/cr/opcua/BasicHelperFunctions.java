package com.kuka.cr.opcua;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.core.BrowsePathTarget;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.RelativePath;
import org.opcfoundation.ua.core.RelativePathElement;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.server.UaServer;

/**
 * This class contains a assortment of helper functions which can be used with
 * the BasicOpcUaServer
 * 
 * @author Venet
 * @author Kaspar
 * @author Bock
 */
public class BasicHelperFunctions {
	private static final Logger log = Logger
			.getLogger(BasicHelperFunctions.class);

	/**
	 * Translates a browse path encoded as string to (possibly several) node
	 * IDs.
	 * 
	 * @param browsePathString
	 *            Browse path to be translated.
	 * @return List of node IDs.
	 * @throws OPCUAException
	 *             if there is a problem with the server connection or
	 *             communication (see cause for details).
	 * @throws NoSuchElementException
	 *             if there is no node ID for the specified browse path.
	 */
	public static List<NodeId> translateBrowsePathToNodeIDs(UaServer server,
			String browsePathString) throws Exception {

		List<RelativePathElement> browsePathEl = new ArrayList<RelativePathElement>();

		for (String s : browsePathString.split("/")) {
			final QualifiedName targetName = QualifiedName
					.parseQualifiedName(s);
			browsePathEl
					.add(new RelativePathElement(
							Identifiers.HierarchicalReferences, false, true,
							targetName));
		}

		RelativePath browsePath = new RelativePath(
				browsePathEl.toArray(new RelativePathElement[0]));

		// The result may always contain several targets (if there are
		// nodes with the same browseName), although normally only one
		// is expected
		BrowsePathTarget[] pathTargets;
		try {
			pathTargets = server.getAddressSpace()
					.translateBrowsePathToNodeIds(Identifiers.RootFolder,
							browsePath);
			List<NodeId> targetIDs = new ArrayList<NodeId>();

			for (BrowsePathTarget pathTarget : pathTargets) {
				NodeId nodeID = NodeId.get(
						pathTarget.getTargetId().getIdType(), pathTarget
								.getTargetId().getNamespaceIndex(), pathTarget
								.getTargetId().getValue());
				targetIDs.add(nodeID);

			}

			if (targetIDs.isEmpty()) {
				final String msg = "No node ID found for browse path "
						+ browsePathString;
				log.error(msg);
				throw new NoSuchElementException(msg);
			}

			return targetIDs;

		} catch (StatusException e) {
			final String msg = "Bad status code returned by the OPC UA server.";
			log.error(msg, e);
			throw new Exception(e);
		} catch (ServiceException e) {
			final String msg = "OPC UA Server error.";
			log.error(msg, e);
			throw new Exception(msg, e);
		}
	}

	/**
	 * Translates a browse path encoded as string to a node IDs.
	 * <p>
	 * Since there may be several node IDs for a single browse path, this method
	 * returns the first in the list of node IDs that are provided by the
	 * server. This should be ok in most cases, however, in case several Node
	 * IDs are expected, call {@link #translateBrowsePathToNodeIDs(String)}, and
	 * handle the resulting list of Node IDs accordingly.
	 * 
	 * @param browsePathString
	 *            Browse path to be translated.
	 * @return Node ID.
	 * @throws OPCUAException
	 *             if there is a problem with the server connection or
	 *             communication (see cause for details).
	 * @throws NoSuchElementException
	 *             if there is no node ID for the specified browse path.
	 */
	static public NodeId translateBrowsePathToNodeID(UaServer server,
			String browsePathString) throws Exception {
		List<NodeId> nodeIDs = translateBrowsePathToNodeIDs(server,
				browsePathString);

		return nodeIDs.get(0);
	}

	/**
	 * Returns the node which is a component of parent and has the BrowseName
	 * browseName, if any.
	 * 
	 * @param parent
	 *            node to be explored
	 * @param browseName
	 *            name to be matched
	 * @return Node
	 */
	public static UaNode getChildNodeByName(UaNode parent, String browseName) {
		UaNode res = _getNodeByName(parent.getComponents(), browseName);
		if (res == null)
			log.error("Could not find a child of ["
					+ parent.getBrowseName().getName() + "] with name ["
					+ browseName + "]");
		return res;
	}

	private static UaNode _getNodeByName(UaNode[] nodes, String browseName) {
		for (UaNode node : nodes)
			if (node.getBrowseName().getName().matches(browseName))
				return node;
		log.error("Could not find a node with name [" + browseName + "]");
		return null;

	}

	/**
	 * Returns the node which, amongst the nodes, has the BrowseName browseName,
	 * if any.
	 * 
	 * @param nodes
	 *            nodes to be explored
	 * @param browseName
	 *            name to be matched
	 * @return Node
	 */
	public static UaNode getNodeByName(UaNode[] nodes, String browseName) {
		UaNode res = _getNodeByName(nodes, browseName);
		if (res == null)
			log.error("Could not find node with name [" + browseName + "]");
		return res;

	}

	static public Boolean isTypeSubtypeOf(UaNode node, NodeId typeNodeId) {
		UaType k = (UaType) node;
		return k.inheritsFrom(typeNodeId);
	}
}
