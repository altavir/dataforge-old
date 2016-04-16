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
package hep.dataforge.points;

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.context.Context;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.DataPointStringIterator;
import hep.dataforge.io.LineIterator;
import hep.dataforge.io.log.Logable;
import hep.dataforge.meta.Laminate;
import hep.dataforge.meta.Meta;
import java.io.IOException;
import java.io.InputStream;

@TypedActionDef(name = "readdataset", inputType = InputStream.class, outputType = PointSet.class, description = "Read DataSet from text file")
@ValueDef(name = "columnNames", multiple = true, info = "The names of columns. By default the first raw is supposed to be name raw")
@ValueDef(name = "encoding", def = "UTF8", info = "file encoding")
@ValueDef(name = "headerLength", type = "NUMBER", info = "The number of header lines to be ignored")
@ValueDef(name = "dataSetName", info = "The name of resulting DataSet. By default the input content name is taken.")
public class ReadPointSetAction extends OneToOneAction<InputStream, PointSet> {

    public static final String READ_DATA_SET_ACTION_NAME = "readdataset";

    /**
     * {@inheritDoc}
     *
     * @param source
     * @return
     */
    @Override
    protected PointSet execute(Context context, Logable log, String name, Laminate meta, InputStream source) {
        ListPointSet fileData;

        String encoding = meta.getString("encoding", "UTF-8");
        try {
            LineIterator iterator = new LineIterator(source, encoding);

            String dataSetName = meta.getString("dataSetName", name);

            DataPointStringIterator dpReader;
            if (meta.hasValue("columnNames")) {
                String[] names = meta.getStringArray("columnNames");
                dpReader = new DataPointStringIterator(iterator, names);
                fileData = new ListPointSet(names);
            } else {
                dpReader = new DataPointStringIterator(iterator, iterator.next());
                fileData = new ListPointSet(dataSetName);
            }

            int headerLines = meta.getInt("headerLength", 0);
            if (headerLines > 0) {
                dpReader.skip(headerLines);
            }

            while (dpReader.hasNext()) {
                fileData.add(dpReader.next());
            }

        } catch (IOException ex) {
            throw new ContentException("Can't open data source");
        }
        return fileData;
    }

}
