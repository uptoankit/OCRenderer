package org.opendaylight.groupbasedpolicy.renderer.oc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.FloatingIpPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2FloodDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.Tenants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2FloodDomain;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class L2DomainManager implements AutoCloseable, DataChangeListener {
	private static final Logger LOG = LoggerFactory
			.getLogger(L2DomainManager.class);
	static ApiConnector apiConnector;
	/*
	 * private static final InstanceIdentifier<L2BridgeDomain> bridgeIid =
	 * InstanceIdentifier .builder(Tenants.class).child(Tenant.class)
	 * .child(L2BridgeDomain.class).build();
	 */

	private static final InstanceIdentifier<L2FloodDomain> floodIid = InstanceIdentifier
			.builder(Tenants.class).child(Tenant.class)
			.child(L2FloodDomain.class).build();

	private ListenerRegistration<DataChangeListener> listenerReg;
	// private ListenerRegistration<DataChangeListener> configListener;

	private final DataBroker dataProvider;

	public L2DomainManager(DataBroker dataProvider,
			RpcProviderRegistry rpcRegistry, ScheduledExecutorService executor) {

		super();
		this.dataProvider = dataProvider;
		if (dataProvider != null) {
			System.out.println("checking for bridgeiid iidd>>>>>>>>>"+ floodIid);
			listenerReg = dataProvider.registerDataChangeListener(
					LogicalDatastoreType.OPERATIONAL, floodIid, this,
					DataChangeScope.ONE);

			/*
			 * configListener =
			 * dataProvider.registerDataChangeListener(LogicalDatastoreType
			 * .CONFIGURATION, bridgeIid, this, DataChangeScope.ONE);
			 */

			// readdata();

		} else
			listenerReg = null;

		LOG.debug("Initialized OC L2 policy manager");
	}

	// // ***************
	// // L2DomainManager
	// // ***************
	//
	// /**
	// * Add a {@link L2DomainManager} to get notifications
	// * @param listener the {@link L2DomainManager} to add
	// */
	// public void registerListener(L2DomainManager listener) {
	// listeners.add(listener);
	// }

	// private void readdata() {
	// // TODO Auto-generated method stub
	// System.out.println("In read data >>>>>>>>>>"
	// + bridgeIid.getPathArguments());
	// if (dataProvider != null) {
	//
	// ListenableFuture<Optional<L2BridgeDomain>> future = dataProvider
	// .newReadOnlyTransaction().read(
	// LogicalDatastoreType.OPERATIONAL, bridgeIid);
	// // Futures.addCallback(future, readSwitchesCallback);
	//
	// future = dataProvider.newReadOnlyTransaction().read(
	// LogicalDatastoreType.CONFIGURATION, bridgeIid);
	// Futures.addCallback(future, readL2DomainInfo);
	// }
	//
	// }

	// private final FutureCallback<Optional<L2BridgeDomain>> readL2DomainInfo =
	// new FutureCallback<Optional<L2BridgeDomain>>() {
	//
	// @Override
	// public void onSuccess(Optional<L2BridgeDomain> result) {
	// // TODO Auto-generated method stub
	//
	// System.out.println("Successsssssss >>>>>>>>>>>>");
	//
	// }
	//
	// @Override
	// public void onFailure(Throwable t) {
	// // TODO Auto-generated method stub
	//
	// System.out.println("Failure >>>>>>>>>>>>>>");
	//
	// }
	// };

	// private class L2BridgeConfigListener implements DataChangeListener {
	//
	// @Override
	// public void onDataChanged(
	// AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
	// System.out.println("in data change method >>>");
	// readdata();
	// }
	// }
	//
	// @Override
	// public void onDataChanged(
	// AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
	// // TODO Auto-generated method stub
	//
	// }

	// ******************
	// DataChangeListener
	// ******************

	@Override
	public void onDataChanged(
			AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
		for (DataObject dao : change.getCreatedData().values()) {
			if (dao instanceof L2FloodDomain)
				System.out.println("data change"+dao);
			
			//			 for(Entry<InstanceIdentifier<?>, DataObject> entry : ((Object) dao).entrySet()){
				 
//			 }
			createNetwork((L2FloodDomain)dao);
			//updateEndpoint(null, (Endpoint)dao);
		}
		// for (InstanceIdentifier<?> iid : change.getRemovedPaths()) {
		// DataObject old = change.getOriginalData().get(iid);
		// if (old != null && old instanceof Endpoint)
		// System.out.println("data change");
		// updateEndpoint((Endpoint)old, null);
		// }
		// Map<InstanceIdentifier<?>,DataObject> d = change.getUpdatedData();
		// for (Entry<InstanceIdentifier<?>, DataObject> entry : d.entrySet()) {
		// if (!(entry.getValue() instanceof Endpoint)) continue;
		// DataObject old = change.getOriginalData().get(entry.getKey());
		// Endpoint oldEp = null;
		// if (old != null && old instanceof Endpoint)
		// oldEp = (Endpoint)old;
		// updateEndpoint(oldEp, (Endpoint)entry.getValue());
		// }
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}
	
	public void createNetwork(L2FloodDomain l2FloodDomain){
		int temp= canCreateFloodDomain(l2FloodDomain);
		
		createFloodDomain(l2FloodDomain);
	}
	
	
	public int canCreateFloodDomain(L2FloodDomain l2FloodDomain) {
        if (l2FloodDomain == null) {
            LOG.error("Network object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        LOG.debug("Network object " + l2FloodDomain);
        apiConnector = OcRenderer.apiConnector;
        LOG.info("apiConnector object in l2domain manager"+apiConnector);
        
        if (l2FloodDomain.getId() == null || l2FloodDomain.getName() == null || l2FloodDomain.getId().equals("")
                || l2FloodDomain.getName().equals("")) {
            LOG.error("Network UUID and Network Name can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
//        if (l2FloodDomain. == null) {
//            LOG.error("Network tenant Id can not be null");
//            return HttpURLConnection.HTTP_BAD_REQUEST;
//        }
        try {
            L2FloodDomainId networkUUID = l2FloodDomain.getId();
            LOG.info("networkUUID >>>>>>"+networkUUID);
            String networkUUIDstring = l2FloodDomain.getId().toString();
            LOG.info("networkUUIDstring >>>>>>"+networkUUIDstring);
            
//            String projectUUID = network.getTenantID();
            try {
                if (!(networkUUIDstring.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }
                boolean isValidNetworkUUID = Utils.isValidHexNumber(networkUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
                if (!isValidNetworkUUID || !isValidprojectUUID) {
                    LOG.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                projectUUID = UUID.fromString(projectUUID).toString();
                networkUUIDstring = UUID.fromString(networkUUIDstring).toString();
            } catch (Exception ex) {
                LOG.error("UUID input incorrect", ex);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            if (project == null) {
                try {
                    Thread.currentThread();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOG.error("InterruptedException :    ", e);
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                project = (Project) apiConnector.findById(Project.class, projectUUID);
                if (project == null) {
                    LOG.error("Could not find projectUUID...");
                    return HttpURLConnection.HTTP_NOT_FOUND;
                }
            }
            VirtualNetwork virtualNetworkById = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (virtualNetworkById != null) {
                LOG.warn("Network already exists with UUID" + networkUUID);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            String virtualNetworkByName = apiConnector.findByName(VirtualNetwork.class, project, network.getNetworkName());
            if (virtualNetworkByName != null) {
                LOG.warn("Network already exists with name : " + virtualNetworkByName);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ie) {
            LOG.error("IOException :   " + ie);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception e) {
            LOG.error("Exception :   " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to create the specified Neutron Network.
     *
     * @param network
     *            An instance of new Neutron Network object.
     */
    private void createFloodDomain(L2FloodDomain l2FloodDomain) throws IOException {
        VirtualNetwork virtualNetwork = new VirtualNetwork();
        // map neutronNetwork to virtualNetwork
        virtualNetwork = mapNetworkProperties(l2FloodDomain, virtualNetwork);
        boolean networkCreated;
        try {
            networkCreated = apiConnector.create(virtualNetwork);
            LOG.debug("networkCreated:   " + networkCreated);
            if (!networkCreated) {
                LOG.warn("Network creation failed..");
            }
        } catch (IOException ioEx) {
            LOG.error("Exception : " + ioEx);
        }
        LOG.info("Network : " + virtualNetwork.getName() + "  having UUID : " + virtualNetwork.getUuid() + "  sucessfully created...");
        if (virtualNetwork.getRouterExternal()) {
            FloatingIpPool floatingIpPool = null;
            String fipId = UUID.randomUUID().toString();
            floatingIpPool = new FloatingIpPool();
            floatingIpPool.setName(fipId);
            floatingIpPool.setDisplayName(fipId);
            floatingIpPool.setUuid(fipId);
            floatingIpPool.setParent(virtualNetwork);
            boolean createFloatingIpPool;
            try {
                createFloatingIpPool = apiConnector.create(floatingIpPool);
                if (!createFloatingIpPool) {
                    LOG.info("Floating Ip pool creation failed..");
                } else {
                    LOG.info("Floating Ip pool created with UUID  : " + floatingIpPool.getUuid());
                }
            } catch (IOException ioEx) {
                LOG.error("IOException : " + ioEx);
            }
        }
    }
}
