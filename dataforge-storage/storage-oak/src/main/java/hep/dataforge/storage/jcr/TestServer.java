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
import hep.dataforge.data.DataPoint;
import hep.dataforge.data.MapDataPoint;
import hep.dataforge.values.Value;
import hep.dataforge.storage.api.DataLoader;
import hep.dataforge.storage.api.PushResult;
import hep.dataforge.storage.api.Storage;
import hep.dataforge.storage.filestorage.FileStorage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.jcr.Session;

/**
 *
 * @author Darksnake
 */
public class TestServer {

    /**
     * @param args the command line arguments
     * @throws hep.dataforge.exceptions.StorageException
     * @throws javax.jcr.RepositoryException
     */
    public static void main(String[] args) throws Throwable {
        Locale.setDefault(Locale.US);
        String[] names = {"key", "2key", "sqrt"};        

        Annotation config = new AnnotationBuilder("myServer")
                .putValue("repository", "D:/Java/jackrabbit/")
                .build();

        try (//        DataServer server = new JackrabbitServer(config);
                Storage server = new FileStorage(config)) {
            if (server instanceof JCRStorage) {
                Session session = ((JCRStorage)server).getSession();
                if (session.itemExists("/data")) {
                    session.getItem("/data").remove();
                    session.save();
                    session.refresh(true);
                }
            }
            
            Annotation loaderConfig = new AnnotationBuilder()
                    //                .putValue("path", "/testrun")
                    .putValue("name", "test")
                    .putValues("names", names)
                    .build();
            
            DataLoader<DataPoint> loader = server.buildLoader(loaderConfig);
            
            
            
            Instant time = Instant.now();
            int n = 10000;
            
            for (int i = 0; i < n; i++) {
                PushResult result = loader.push(new MapDataPoint(names, i, i * 2, Math.sqrt(i)));
                if (!result.isSuccsess()) {
                    System.out.printf("%nPoint with number %d loader failed with message: %n%n", i, result.toString());
                    throw result.getError();
                }
            }
            
            Instant newTime = Instant.now();
            
            long millis = newTime.toEpochMilli() - time.toEpochMilli();
            
            System.out.printf("%nЗапись %d элементов завершена за %f секунд; %g с на элемент%n", n, (double) (millis) / 1000d, (double) (millis) / 1000d / n);
            time = newTime;
            
            List<DataPoint> set = loader.pullRange("key", Value.of(1300), Value.of(1400));
            
            newTime = Instant.now();
            millis = newTime.toEpochMilli() - time.toEpochMilli();
            System.out.printf("%nЧтение сплошного сегмента из середины набора данных длинной в %d завершено за %f секунд%n",
                    set.size(), (double) (millis) / 1000d);
            time = newTime;
            
//        new ColumnedDataWriter(System.out, names).writeDataSet(set, "*** MY DATA ***");
            Random generator = new Random();
            
            int k = 100;
            
            set = new ArrayList<>(k);
            
            for (int i = 0; i < k; i++) {
                set.add(loader.pullSingle("key", Value.of(generator.nextInt(n - 1))));
            }
            
            newTime = Instant.now();
            millis = newTime.toEpochMilli() - time.toEpochMilli();
            System.out.printf("%nЧтение случайного набора из %d точек завершено за %f секунд%n",
                    set.size(), (double) (millis) / 1000d);
            
            System.out.println();
//        System.out.printf("The total number of elements in directory is %d%n", loader.size());
        }

    }

}
