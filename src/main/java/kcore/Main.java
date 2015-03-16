/**
 * Created by Stefano on 04/02/2015.
 */
package kcore;

import Pi.MetricsListener;
import Pi.PiBackend;
import Pi.PiFrontend;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;

import com.typesafe.config.*;

import javax.swing.*;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

public class Main {
    public static void main(String[] args){
        final Config confstr;
        if(args.length > 1) {
            confstr = readParamsFromCli(args);
        } else {
            confstr = readParamsFromGui();

        }
        final Config defaultConfig = ConfigFactory.load("default");
        if (confstr != null) {
            final Config config = confstr.withFallback(defaultConfig);
            fireUpActorSystem(config);
        } else {
            final Config config2 = ConfigFactory.parseResourcesAnySyntax("test2").withFallback(defaultConfig);
            fireUpActorSystem(config2, "k-core");
            final Config config1 = ConfigFactory.parseResourcesAnySyntax("test1").withFallback(defaultConfig);
            fireUpActorSystem(config1, "k-core");

        }
    }

    private static void fireUpActorSystem(Config config) {
        fireUpActorSystem(config, "k-core");
    }

    private static void fireUpActorSystem(Config config, String kcorename) {
        final ActorSystem sys = ActorSystem.create(kcorename, config);
        sys.log().debug("actor system initialized");


        final Cluster cluster = Cluster.get(sys);
        final Set<String> roles = cluster.getSelfRoles();
        if (roles.isEmpty()){
            sys.log().error("no role specified for node");
            sys.shutdown();
            return;
        }
        cluster.registerOnMemberUp(new Runnable() {
            @Override
            public void run() {
                sys.actorOf(Props.create(MetricsListener.class), "metricsListener");

                if (roles.contains("frontend")){
                    sys.actorOf(Props.create(PiFrontend.class), "piFrontend");
                }

                if (roles.contains("backend")){
                    sys.actorOf(Props.create(PiBackend.class), "piBackend");
                }
            }
        });
    }

    private static Config readParamsFromCli(String[] args) {
        if (args[1] != "test")
            return ConfigFactory.parseFile(new File(args[1]));
        else
            return null;
    }

    private static Config readParamsFromGui() {
        String confstr;
        String[] roleStr = {"frontend", "backend"};
        JComboBox roles = new JComboBox(roleStr);
        Vector<String> ipStr = new Vector<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()){
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while(inetAddresses.hasMoreElements()){
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
                JOptionPane.showConfirmDialog(null, comps, "akka parameters", JOptionPane.OK_CANCEL_OPTION)){
            confstr = String.format(
                    "akka.cluster.roles=[%s]\nakka.remote.netty.tcp.hostname=\"%s\"\nakka.cluster.seed-nodes=[\"akka.tcp://k-core@%s:25515\", \"akka.tcp://k-core@%s:25515\"]",
                    roles.getSelectedItem().toString(), ips.getSelectedItem().toString(), ips.getSelectedItem().toString(), seed.getText());

        } else {
            return null;
        }
        return ConfigFactory.parseString(confstr);
    }
}
