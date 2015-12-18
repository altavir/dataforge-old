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
package hep.dataforge.description;

import hep.dataforge.description.ActionDescriptor;
import hep.dataforge.meta.Meta;
import hep.dataforge.values.Value;
import hep.dataforge.values.ValueType;
import hep.dataforge.exceptions.DescriptorException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * <p>
 * TextDescriptorFormatter class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class TextDescriptorFormatter implements DescriptorFormatter {

    /**
     * Constant <code>ANSI_RESET="\u001B[0m"</code>
     */
    public static final String ANSI_RESET = "\u001B[0m";
    /**
     * Constant <code>ANSI_BLACK="\u001B[30m"</code>
     */
    public static final String ANSI_BLACK = "\u001B[30m";
    /**
     * Constant <code>ANSI_RED="\u001B[31m"</code>
     */
    public static final String ANSI_RED = "\u001B[31m";
    /**
     * Constant <code>ANSI_GREEN="\u001B[32m"</code>
     */
    public static final String ANSI_GREEN = "\u001B[32m";
    /**
     * Constant <code>ANSI_YELLOW="\u001B[33m"</code>
     */
    public static final String ANSI_YELLOW = "\u001B[33m";
    /**
     * Constant <code>ANSI_BLUE="\u001B[34m"</code>
     */
    public static final String ANSI_BLUE = "\u001B[34m";
    /**
     * Constant <code>ANSI_PURPLE="\u001B[35m"</code>
     */
    public static final String ANSI_PURPLE = "\u001B[35m";
    /**
     * Constant <code>ANSI_CYAN="\u001B[36m"</code>
     */
    public static final String ANSI_CYAN = "\u001B[36m";
    /**
     * Constant <code>ANSI_WHITE="\u001B[37m"</code>
     */
    public static final String ANSI_WHITE = "\u001B[37m";

    private final PrintWriter writer;
    private final boolean allowANSI;

    /**
     * <p>
     * Constructor for TextDescriptorFormatter.</p>
     *
     * @param writer a {@link java.io.PrintWriter} object.
     * @param allowANSI a boolean.
     */
    public TextDescriptorFormatter(PrintWriter writer, boolean allowANSI) {
        this.writer = writer;
        this.allowANSI = allowANSI;
    }

    public TextDescriptorFormatter(PrintWriter writer) {
        this.writer = writer;
        allowANSI = !System.getProperty("os.name").contains("Windows");
    }

    public TextDescriptorFormatter(OutputStream stream) {
        this.writer = new PrintWriter(stream);
        allowANSI = !System.getProperty("os.name").contains("Windows");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showDescription(NodeDescriptor descriptor) throws DescriptorException {
        showShortDescription(descriptor);
    }

    private String wrapANSI(String str, String ansiColor) {
        if (allowANSI) {
            return ansiColor + str + ANSI_RESET;
        } else {
            return str;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showShortDescription(NodeDescriptor descriptor) throws DescriptorException {
        showDescription("\t", descriptor);
    }

    public void showDescription(String prefix, NodeDescriptor descriptor) throws DescriptorException {
        printDescriptorHead(descriptor);
        Meta description = descriptor.meta();
        printNodeContent(prefix, description);
    }

    protected void printDescriptorHead(NodeDescriptor descriptor) {
        if (descriptor instanceof ActionDescriptor) {
            ActionDescriptor ad = (ActionDescriptor) descriptor;
            writer.printf("%s {input : %s, output : %s}: %s%n",
                    wrapANSI(ad.getName(), ANSI_GREEN),
                    wrapANSI(ad.inputType(), ANSI_CYAN),
                    wrapANSI(ad.outputType(), ANSI_CYAN),
                    ad.info());
        } else if (descriptor.getName() != null && !descriptor.getName().isEmpty()) {
            writer.println(wrapANSI(descriptor.getName(), ANSI_BLUE));
        }
    }

    protected void printParameterShort(String prefix, Meta valueDef) throws DescriptorException {
        writer.printf(prefix);

        writer.printf("- ");

        if (valueDef.getBoolean("required", false)) {
            writer.printf("(*) ");
        }

        writer.printf(wrapANSI(valueDef.getString("name"), ANSI_RED));

        if (valueDef.getBoolean("multiple", false)) {
            writer.print(" (mult)");
        }

        writer.printf(" (%s)", valueDef.getString("type", "STRING"));

        if (valueDef.hasValue("def")) {
            Value def = valueDef.getValue("default");
            if (def.valueType().equals(ValueType.STRING)) {
                writer.printf(" = \"%s\": ", wrapANSI(def.stringValue(), ANSI_YELLOW));
            } else {
                writer.printf(" = %s: ", wrapANSI(def.stringValue(), ANSI_YELLOW));
            }
        } else {
            writer.print(": ");
        }

        writer.printf("%s", valueDef.getString("info"));
        writer.println();
        writer.flush();
    }

    protected void printNodeContent(String prefix, Meta nodeDef) throws DescriptorException {
        if (nodeDef.hasNode("node")) {
            for (Meta elDef : nodeDef.getNodes("node")) {
                printElementShort(prefix, elDef);
            }
        }

        if (nodeDef.hasNode("value")) {
            for (Meta parDef : nodeDef.getNodes("value")) {
                printParameterShort(prefix, parDef);
            }
        }
    }

    protected void printElementShort(String prefix, Meta elementDef) throws DescriptorException {
        writer.printf(prefix);
        writer.printf("+ ");

        if (elementDef.getBoolean("required", false)) {
            writer.printf("(*) ");
        }
        writer.printf(wrapANSI(elementDef.getString("name"), ANSI_PURPLE));

        if (elementDef.getBoolean("multiple", false)) {
            writer.print(" (mult)");
        }

        writer.print(": ");

        writer.printf("%s", elementDef.getString("info"));
        writer.println();

        printNodeContent("\t" + prefix, elementDef);
        
        writer.flush();
    }
}
