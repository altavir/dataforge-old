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
package hep.dataforge.data;

import hep.dataforge.actions.OneToOneAction;
import hep.dataforge.meta.Meta;
import hep.dataforge.description.TypedActionDef;
import hep.dataforge.description.ValueDef;
import hep.dataforge.context.Context;
import hep.dataforge.values.ValueType;
import hep.dataforge.exceptions.ContentException;
import hep.dataforge.io.DataPointStringIterator;
import hep.dataforge.io.LineIterator;
import hep.dataforge.io.log.Logable;
import java.io.IOException;

/**
 * <p>
 * ReadDataSetAction class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
@TypedActionDef(name = "readdataset", inputType = BinaryData.class, outputType = DataSet.class, description = "Read DataSet from text file")
@ValueDef(name = "columnNames", multiple = true, info = "The names of columns. By default the first raw is supposed to be name raw")
@ValueDef(name = "encoding", def = "UTF8", info = "file encoding")
@ValueDef(name = "headerLength", type = "NUMBER", info = "The number of header lines to be ignored")
@ValueDef(name = "dataSetName", info = "The name of resulting DataSet. By default the input content name is taken.")
public class ReadDataSetAction extends OneToOneAction<BinaryData, DataSet> {

    /**
     * Constant <code>READ_DATA_SET_ACTION_NAME="readdataset"</code>
     */
    public static final String READ_DATA_SET_ACTION_NAME = "readdataset";

    /**
     * <p>
     * Constructor for ReadDataSetAction.</p>
     *
     * @param context a {@link hep.dataforge.context.Context} object.
     * @param an a {@link hep.dataforge.meta.Meta} object.
     */
    public ReadDataSetAction(Context context, Meta an) {
        super(context, an);
    }

    /**
     * {@inheritDoc}
     *
     * @param source
     * @return
     */
    @Override
    protected DataSet execute(Logable log, Meta meta, BinaryData source) {
        ListDataSet fileData;

        String encoding = source.meta().getString("encoding", meta.getString("encoding"));
        try {
            LineIterator iterator = new LineIterator(source.getInputStream(), encoding);

            String dataSetName = meta.getString("dataSetName", source.getName());
            
            DataPointStringIterator dpReader;
            if (meta().hasValue("columnNames")) {
                String[] names = meta().getStringArray("columnNames");
                dpReader = new DataPointStringIterator(iterator, names);
                fileData = new ListDataSet(dataSetName, names);
            } else {
                dpReader = new DataPointStringIterator(iterator, iterator.next());
                fileData = new ListDataSet(dataSetName);
            }

            int headerLines = meta().getInt("headerLength", 0);
            if (headerLines > 0) {
                dpReader.skip(headerLines);
            }

            while (dpReader.hasNext()) {
                fileData.add(dpReader.next());
            }

        } catch (IOException ex) {
            throw new ContentException("Can't open data source");
        }
        fileData.configure(source.meta());
        return fileData;
    }

}
