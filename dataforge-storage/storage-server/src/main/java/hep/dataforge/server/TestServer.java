/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and read the template in the editor.
 */
package hep.dataforge.server;

import hep.dataforge.context.Context;
import hep.dataforge.context.Global;
import hep.dataforge.server.storage.StorageServerUtils;
import hep.dataforge.storage.commons.StorageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Alexander Nozik
 */
public class TestServer {

    /**
     * @param args the command line arguments
     * @throws hep.dataforge.exceptions.StorageException
     */
    public static void main(String[] args) throws Exception {
        Context context = Global.INSTANCE.getContext("SERVER-TEST");
        StorageManager storageManager = context.getPluginManager().load(StorageManager.class);

        ServerManager serverManager = context.getPluginManager().load(ServerManager.class);

        String path = "D:/temp/test";
        System.out.println("Starting test numass storage servlet in " + path);
        StorageServerUtils.addFileStorage(serverManager, path, "test");

        serverManager.startServer();

        String stopLine = "";

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (stopLine == null || !stopLine.startsWith("exit")) {
            //    print ">"
            stopLine = br.readLine();
        }

        System.out.println("Stopping ratpack");
        serverManager.stopServer();
        Global.INSTANCE.terminate();
    }

}
