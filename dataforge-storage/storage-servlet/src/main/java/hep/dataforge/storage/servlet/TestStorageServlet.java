/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and read the template in the editor.
 */
package hep.dataforge.storage.servlet;

import hep.dataforge.exceptions.StorageException;
import hep.dataforge.storage.commons.StoragePlugin;
import hep.dataforge.storage.filestorage.FileStorage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import ratpack.handling.Chain;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerSpec;
import ratpack.server.ServerConfigBuilder;

/**
 *
 * @author Alexander Nozik
 */
public class TestStorageServlet {

    /**
     * @param args the command line arguments
     * @throws hep.dataforge.exceptions.StorageException
     */
    public static void main(String[] args) throws StorageException, Exception {
        new StoragePlugin().startGlobal();
        String path = "/home/numass-storage";

        FileStorage storage = FileStorage.connect(new File(path), true, false);

        RatpackServer ratpack = RatpackServer.start((RatpackServerSpec server) -> server
                .serverConfig((ServerConfigBuilder config) -> config.port(8337))
                .handlers((Chain chain) -> chain
                        .get("storage", new SorageRatpackHandler(storage))
                )
        );
        System.out.println("Starting test numass storage servlet in " + path);
        String stopLine = "";

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (stopLine == null || !stopLine.startsWith("exit")) {
            //    print ">"
            stopLine = br.readLine();
        }
        System.out.println("Stopping ratpack");
        if (ratpack != null && ratpack.isRunning()) {
            try {
                ratpack.stop();
                storage.close();
            } catch (Exception ex) {
                System.out.println("Failed to stop ratpack server");
            }
        }
    }

}
