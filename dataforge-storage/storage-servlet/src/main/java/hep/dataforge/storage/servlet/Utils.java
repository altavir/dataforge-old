/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.storage.servlet;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.ibm.icu.util.GregorianCalendar;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import hep.dataforge.data.Format;
import hep.dataforge.data.DataPoint;
import hep.dataforge.values.ColumnFormat;
import hep.dataforge.values.ValueFormat;
import java.util.Date;
import org.slf4j.LoggerFactory;
import hep.dataforge.data.PointSet;

/**
 *
 * @author Alexander Nozik
 */
public class Utils {
    
    private static Configuration freemarkerConfig;
    
    public static Configuration freemarkerConfig() {
        if (freemarkerConfig == null) {
            freemarkerConfig = new Configuration(Configuration.VERSION_2_3_23);
            freemarkerConfig.setClassForTemplateLoading(Utils.class, "/templates");
            freemarkerConfig.setDefaultEncoding("UTF-8");
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }
        return freemarkerConfig;
    }
    
    public static DataTable generateDataTable(PointSet data) {
        DataTable table = new DataTable();
        Format format = data.getDataFormat();
        for (String name : format) {
            ValueFormat vf = format.getValueFormat(name);
            ValueType vt;
            if (vf instanceof ColumnFormat) {
                ColumnFormat cf = (ColumnFormat) vf;
                switch (cf.primaryType()) {
                    case NUMBER:
                        vt = ValueType.NUMBER;
                        break;
                    case BOOLEAN:
                        vt = ValueType.BOOLEAN;
                        break;
                    default:
                        vt = ValueType.TEXT;
                }
            } else {
                vt = ValueType.TEXT;
            }
            table.addColumn(new ColumnDescription(name, vt, name));
        }
        
        for (DataPoint p : data) {
            TableRow row = new TableRow();
            for (String name : format) {
                row.addCell(toGoogleValue(p.getValue(name)));
            }
            try {
                table.addRow(row);
            } catch (TypeMismatchException ex) {
                LoggerFactory.getLogger(Utils.class).error("Wrong content for dataset transformation to google visualization", ex);
            }
        }
        return table;
    }
    
    private static Value toGoogleValue(hep.dataforge.values.Value val) {
        switch (val.valueType()) {
            case NUMBER:
                return new NumberValue(val.doubleValue());
            case BOOLEAN:
                return val.booleanValue() ? BooleanValue.TRUE : BooleanValue.FALSE;
            case TIME:
                GregorianCalendar gr = new GregorianCalendar();
                gr.setTime(Date.from(val.timeValue()));
                return new DateTimeValue(gr);
            default:
                return new TextValue(val.stringValue());
        }
    }
}
