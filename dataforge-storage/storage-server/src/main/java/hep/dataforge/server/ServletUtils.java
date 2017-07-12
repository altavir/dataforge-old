/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.server;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import ratpack.handling.Context;

/**
 *
 * @author Alexander Nozik
 */
public class ServletUtils {
    
    private static Configuration freemarkerConfig;
    
    public static Configuration freemarkerConfig() {
        if (freemarkerConfig == null) {
            freemarkerConfig = new Configuration(Configuration.VERSION_2_3_23);
            freemarkerConfig.setClassForTemplateLoading(ServletUtils.class, "/templates");
            freemarkerConfig.setDefaultEncoding("UTF-8");
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }
        return freemarkerConfig;
    }

    public static String getServerURL(Context ctx){
        String hostName;
        if (ctx.getServerConfig().getAddress() != null) {
            hostName = ctx.getServerConfig().getAddress().getHostAddress();
        } else {
            hostName = "localhost";
        }
        return "http://" + hostName + ":" + ctx.getServerConfig().getPort();
    }

//    public static DataTable generateDataTable(PointSource source) {
//        DataTable table = new DataTable();
//        TableFormat format = source.getFormat();
//        for (String name : format.names()) {
//            ValueFormatter vf = format.getValueFormat(name);
//            ValueType vt;
//            if (vf instanceof ColumnFormat) {
//                ColumnFormat cf = (ColumnFormat) vf;
//                switch (cf.primaryType()) {
//                    case NUMBER:
//                        vt = ValueType.NUMBER;
//                        break;
//                    case BOOLEAN:
//                        vt = ValueType.BOOLEAN;
//                        break;
//                    default:
//                        vt = ValueType.TEXT;
//                }
//            } else {
//                vt = ValueType.TEXT;
//            }
//            table.addColumn(new ColumnDescription(name, vt, name));
//        }
//
//        for (DataPoint p : source) {
//            TableRow row = new TableRow();
//            for (String name : format.names()) {
//                row.addCell(toGoogleValue(p.getValue(name)));
//            }
//            try {
//                table.addRow(row);
//            } catch (TypeMismatchException ex) {
//                LoggerFactory.getLogger(ServletUtils.class).error("Wrong content for dataset transformation to google visualization", ex);
//            }
//        }
//        return table;
//    }
    
//    private static Value toGoogleValue(hep.dataforge.values.Value val) {
//        switch (val.getType()) {
//            case NUMBER:
//                return new NumberValue(val.doubleValue());
//            case BOOLEAN:
//                return val.booleanValue() ? BooleanValue.TRUE : BooleanValue.FALSE;
//            case TIME:
//                GregorianCalendar gr = new GregorianCalendar();
//                gr.setTime(Date.from(val.timeValue()));
//                return new DateTimeValue(gr);
//            default:
//                return new TextValue(val.stringValue());
//        }
//    }
}
