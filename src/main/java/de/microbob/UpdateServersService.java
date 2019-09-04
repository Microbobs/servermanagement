package de.microbob;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.microbob.constant.OperationSystem;
import de.microbob.constant.ServerStatus;
import de.microbob.constant.ServerTyp;
import de.microbob.exception.NotImplementedException;
import de.microbob.model.Server;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateServersService extends ScheduledService<Void> {

    private List<Server> servers;

    private TableView<Server> serverTV;

    public UpdateServersService(List<Server> servers, TableView<Server> serverTV) {
        this.servers = servers;
        this.serverTV = serverTV;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                long startMillis = System.currentTimeMillis();
                System.out.println("Refresh Thread: " + Thread.currentThread().getName());

                for (Server server : servers) {
                    ServerStatus status = getStatusForServer(server);
                    server.setStatus(status);
                    String webapps = getWebappsStringForServer(server);
                    server.setWebapps(webapps);
                }
                System.out.println("Refresh Duration after Status-Check: " + (System.currentTimeMillis() - startMillis) / 1000F + " sec");

                serverTV.refresh();

                System.out.println("Refresh Duration: " + (System.currentTimeMillis() - startMillis) / 1000F + " sec");
                return null;
            }
        };
    }

    private ServerStatus getStatusForServer(Server server) {
        System.out.println("getStatusForServer Thread: " + Thread.currentThread().getName());
        ServerStatus status = ServerStatus.UNKNOWN;

        String port = server.getPort();

        if (port != null) {
            try {
                Socket socket = new Socket();
                String hostname = server.isLocal() ? "127.0.0.1" : server.getPfad();
                socket.connect(new InetSocketAddress(hostname, Integer.parseInt(port)), 500);
                status = ServerStatus.RUNNING;
            } catch (IOException e) {
                status = ServerStatus.STOPPED;
            }
        }

        return status;
    }

    private String getWebappsStringForServer(Server server) throws IOException, JSchException {
        String webappsString = "";
        if (server.isLocal()) {
            Path pathToWebappsDir = null;

            switch (server.getTyp()) {
                case TOMCAT:
                    pathToWebappsDir = Paths.get(server.getPfad(), "/webapps");
                    break;
                case WILDFLY:
                    pathToWebappsDir = Paths.get(server.getPfad(), "/standalone/deployments");
                    break;
            }

            if (pathToWebappsDir != null) {
                List<String> webappFiles = Files.list(pathToWebappsDir)
                        .filter(p -> Files.isDirectory(p))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());

                webappsString = String.join(", ", webappFiles);
            }
        } else {
            Session session = MainApplication.getSessionForServer().get(server);

            if (session == null || !session.isConnected()) {
                JSch jSch = new JSch();

                String username = server.getBenutzer();
                String password = server.getPasswort();

                session = jSch.getSession(username, server.getPfad(), 22);
                session.setPassword(password);
                session.setUserInfo(new MainApplication.FxUserInfo());
                session.connect();
                MainApplication.getSessionForServer().put(server, session);
            }

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            ServerTyp typ = server.getTyp();
            OperationSystem os = server.getOperationSystem();
            String command = "";

            boolean unix = false;
            switch (typ) {
                case TOMCAT:
                    switch (os) {
                        case WIN:
                            //TODO
                            throw new NotImplementedException();
                        case UNIX:
                            command = "ls -d " + Paths.get(server.getRemotePfad(), "/webapps").toString().replaceAll("\\\\", "/") + "/*/";
                            unix = true;
                            break;
                    }
                    break;
                case WILDFLY:
                    switch (os) {
                        case WIN:
                            //TODO
                            throw new NotImplementedException();
                        case UNIX:
                            command = "ls -d " + Paths.get(server.getRemotePfad(), "/standalone/deployments").toString()
                                    .replaceAll("\\\\", "/") + "/*/";
                            unix = true;
                            break;
                    }
                    break;
            }

            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            webappsString = getWebappsFromCommandResult(channel, unix);
        }

        return webappsString;
    }

    private String getWebappsFromCommandResult(ChannelExec channel, boolean unix) throws JSchException, IOException {
        String webappsString = "";

        BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        channel.connect();

        StringBuilder allLines = new StringBuilder();
        while (true) {
            String line = in.readLine();
            while (line != null) {
                allLines.append(line);
                line = in.readLine();
            }

            if (channel.isClosed()) {
                if (in.ready()) {
                    continue;
                }
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(500);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        channel.disconnect();


        if (unix) {
            String[] paths = allLines.toString().split("//");
            webappsString = Arrays.stream(paths).map(p ->
            {
                String[] dirNames = p.split("/");
                return dirNames[dirNames.length - 1];
            }).collect(Collectors.joining(", "));
        }

        return webappsString;
    }
}
