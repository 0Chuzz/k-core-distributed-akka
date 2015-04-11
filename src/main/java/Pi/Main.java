/**
 * Created by Stefano on 04/02/2015.
 */
package Pi;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.swing.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        String confstr;
        if (args.length > 1) {
            confstr = args[1];
        } else {
            confstr = readParamsFromGui();

        }
        final Config defaultConfig = ConfigFactory.load("default");
        final Config config = ConfigFactory.parseString(confstr).withFallback(defaultConfig);
        final ActorSystem sys = ActorSystem.create("k-core", config);
        sys.log().debug("parameters: {}", confstr);
        sys.log().debug("actor system initialized");


        final Cluster cluster = Cluster.get(sys);
        final Set<String> roles = cluster.getSelfRoles();
        if (roles.isEmpty()) {
            sys.log().error("no role specified for node");
            sys.shutdown();
            return;
        }
        cluster.registerOnMemberUp(new Runnable() {
            @Override
            public void run() {
                sys.actorOf(Props.create(MetricsListener.class), "metricsListener");

                if (roles.contains("frontend")) {
                    sys.actorOf(Props.create(PiFrontend.class), "piFrontend");
                }

                if (roles.contains("backend")) {
                    sys.actorOf(Props.create(PiBackend.class), "piBackend");
                }
            }
        });
    }

    private static String readParamsFromGui() {
        String confstr;
        String[] roleStr = {"frontend", "backend"};
        JComboBox roles = new JComboBox(roleStr);
        Vector<String> ipStr = new Vector<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress ip = inetAddresses.nextElement();
                    ipStr.add(ip.getHostAddress());
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        JComboBox ips = new JComboBox(ipStr);
        JTextField seed = new JTextField("seed-node-ip");
        final JComponent[] comps = {roles, ips, seed};
        JOptionPane jp = new JOptionPane(comps);
        if (JOptionPane.OK_OPTION ==
                JOptionPane.showConfirmDialog(null, comps, "akka parameters", JOptionPane.OK_CANCEL_OPTION)) {
            confstr = String.format(
                    "akka.cluster.roles=[%s]\nakka.remote.netty.tcp.hostname=\"%s\"\nakka.cluster.seed-nodes=[\"akka.tcp://k-core@%s:25515\", \"akka.tcp://k-core@%s:25515\"]",
                    roles.getSelectedItem().toString(), ips.getSelectedItem().toString(), ips.getSelectedItem().toString(), seed.getText());

        } else {
            confstr = "";
        }
        return confstr;
    }
}
