package com.sdp;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * SDP Feb, 2018
 * Coding challenge for hazelcast.
 *
 * I decided to make use of your product to solve the challenge. Hopefully, I will get kudos
 * for demonstrating my knowledge of your product as a side benefit to solving the challenge.
 * Thanks for your interest in me.
 *
 * my solution:
 *
 * 1) Attempt to start a hazelcast client, if successful, print size of data map and exit
 * 2) Client cant connect, startup a cluster node using UDP networking
 * 3) Check for "data1" map contents
 * 4) If empty, add element to shared "data1" map. Then use System.out.println to print
 *    the challenge's required message once / cluster / app start
 * 5) If "data1" map is not empty, add another element
 * 6) Sleep for an hour
 *
 * Notes: There may be many cluster nodes created, but there will only be one printing of the
 *   challenge message because of the key added into the shared map.
 *
 * I am having some mixed results getting the hazelcast client to connect between distinct
 * hosts. At home, I only have one computer, an ASUS laptop, so the best I can do regarding
 * multiple hosts connecting is to use VirtualBox to run a Ubuntu VPC. If I had more hardware
 * to hand, I could do more testing. However, I believe that even given the current implementation
 * the logic works correctly as required.
 */
public class CodeChallenge {

    public static final String INSTANCE_NAME = "sdpInstance";
    public static final String MAP_NAME = "data1";

    // You will need to change both of these for your environment
    public static final String CLIENT_BIND_ADDRESS = "192.168.1.3";
    public static final String CLUSTER_BIND_INTERFACE = "192.168.1.*";

    public static void main( String[] args ) throws Exception {

        // SDP - for this challenge, coding a client which attempts first to join a cluster is overkill
        // however, no constraints were specified and I thought you might appreciate implementing
        // the challenge using a client and server (member) combination
        final HazelcastInstance client = clientAttemptToJoinCluster();

        if( client != null ) {
            // we joined. report size of shared map, wait for a few seconds then exit
            System.out.println( "Joined cluster. Map size: " + client.getMap( MAP_NAME ).size() );
            Thread.sleep( 5000 );
            client.shutdown();
        }
        else {
            // prepare Hazelcast cluster/member instance
            HazelcastInstance hazelcastInstance = buildCluster();

            IMap<Long, String> map = hazelcastInstance.getMap( MAP_NAME );
            if( map.size() == 0 ) {
                System.out.println("We are started!"); // <--- the challenge required message
            }
            map.put( System.currentTimeMillis(), String.valueOf( getNodeSignature() ) );

            System.out.println( "Sleeping for an hour... " + getNodeSignature() );
            Thread.sleep( 60 * 60 * 1000 ); // an hour
        }
        Hazelcast.shutdownAll();
    }

    private static HazelcastInstance clientAttemptToJoinCluster() {
        HazelcastInstance client = null;
        try {
            final ClientConfig clientConfig = new ClientConfig();
            clientConfig.setProperty("hazelcast.socket.client.bind.any", "true");
            clientConfig.setInstanceName( INSTANCE_NAME );
            clientConfig.getNetworkConfig().addAddress( CLIENT_BIND_ADDRESS );

            client = HazelcastClient.newHazelcastClient( clientConfig );
        } catch( IllegalStateException ex ) {
            System.err.println( "Client cannot start. No cluster to join" + ex.getMessage() );
        }
        return client;
    }

    private static String getNodeSignature() throws UnknownHostException {
        InetAddress ip = InetAddress.getLocalHost();
        return ip.getHostName() + " " + ip + " " + new Date();
    }

    private static HazelcastInstance buildCluster() throws InterruptedException {
        final Config config = new Config();
        config.setProperty("hazelcast.socket.server.bind.any", "false");
        config.setInstanceName( INSTANCE_NAME );
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled( true );
        config.getNetworkConfig().getInterfaces().setEnabled( true );
        config.getNetworkConfig().getInterfaces().addInterface( CLUSTER_BIND_INTERFACE );

        // Find UDP & TCP config details here:
        // http://docs.hazelcast.org/docs/latest-development/manual/html/Setting_Up_Clusters/Other_Network_Configurations.html
        // https://stackoverflow.com/questions/38524637/hazelcast-network-interfaces
        final MulticastConfig mcConfig = config.getNetworkConfig().getJoin().getMulticastConfig();
        mcConfig.setMulticastGroup( "224.2.2.3" );
        mcConfig.setMulticastPort( 9191 );
        mcConfig.setMulticastTimeToLive( 32 );

        return Hazelcast.getOrCreateHazelcastInstance( config );
    }
}
