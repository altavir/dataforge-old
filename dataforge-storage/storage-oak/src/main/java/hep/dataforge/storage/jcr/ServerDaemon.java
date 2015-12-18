/* 
 * Copyright 2015 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hep.dataforge.storage.jcr;

import hep.dataforge.annotations.Annotation;
import hep.dataforge.annotations.AnnotationBuilder;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorage;
import hep.dataforge.storage.remote.NetworkListner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 *
 * @author Darksnake
 */
public class ServerDaemon implements Daemon {

    NetworkListner listner;
    Storage server;

    @Override
    public void destroy() {
        System.exit(0);
    }

    @Override
    public void init(DaemonContext dc) throws DaemonInitException, Exception {
        init(dc.getArguments());
    }

    public void init(String[] args) {
        setupLogger();

        Options options = prepareOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine line;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException ex) {
            System.err.println("Error in the command line");
            throw new RuntimeException(ex);
        }

        AnnotationBuilder builder = new AnnotationBuilder("config");

        if (line.hasOption("r")) {
            builder.putValue("repository", line.getOptionValue("r"));
        }

        if (line.hasOption("p")) {
            builder.putValue("port", line.getOptionValue("r"));
        }

        Annotation config = builder.build();
        
        String type = line.getOptionValue("t", "FILE");
        switch(type){
            case "JCR":
                server = new JackrabbitServer(config);
                break;
            case "FILE":
                server = new FileStorage(config);
                break;
            case "OAK":
                System.err.println("OAK server is not yet supported. Using Jackrabbit instead.");
                server = new JackrabbitServer(config);
                break;
            default:
                throw new RuntimeException("Unknown server type");
        }
        
        listner = new NetworkListner(server, AnnotationBuilder.buildEmpty(null));
    }

    @Override
    public void start() throws Exception {
        server.open();
        listner.start();
    }

    @Override
    public void stop() throws Exception {
        listner.close();
        server.close();
    }

    private Options prepareOptions() {
        Options options = new Options();

        options.addOption("r", "repository", true, "Repository path");
        options.addOption("p", "port", true, "Listning port");
        options.addOption("t", "type", true, "Repository type (FILE, JCR, OAK)");

        return options;
    }

    private static void setupLogger() {
        final org.slf4j.Logger logger
                = org.slf4j.LoggerFactory.getLogger("org.appache.jackrabbit");
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger
                    = (ch.qos.logback.classic.Logger) logger;
            logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
        }
    }

    public static void main(String[] args) throws Exception {
        ServerDaemon daemon = new ServerDaemon();
        daemon.init(args);
        daemon.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            if ("exit".equals(br.readLine())) {
                daemon.stop();

                break;
            }
        }
        daemon.destroy();
    }
}
