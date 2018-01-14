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
package hep.dataforge.context;

import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.meta.SimpleMetaMorph;

/**
 * The tag which contains information about name, group and version of some
 * object. It also could contain any complex rule to define version ranges
 *
 * @author Alexander Nozik
 */
//@ValueDef(name = "role", multiple = true,info = "The list of roles this plugin implements")
//@ValueDef(name = "priority", type = "NUMBER", info = "Plugin load priority. Used for plugins with the same role")
public class PluginTag extends SimpleMetaMorph {

    /**
     * Build new PluginTag from standard string representation
     *
     * @param tag
     * @return
     */
    public static PluginTag fromString(String tag) {
        int sepIndex = tag.indexOf(":");
        if (sepIndex >= 0) {
            return new PluginTag(tag.substring(0, sepIndex), tag.substring(sepIndex + 1));
        } else {
            return new PluginTag("", tag);
        }
    }

    public PluginTag(Meta meta) {
        super(meta);
    }

    public PluginTag(String group, String name) {
        super(new MetaBuilder("plugin")
                .setValue("group", group)
                .setValue("name", name)
        );
    }

    public String getName() {
        return getString("name", "");
    }

    public String getGroup() {
        return getString("group", "");
    }


    /**
     * Check if given tag is compatible (in range) of this tag
     *
     * @param otherTag
     * @return
     */
    public boolean matches(PluginTag otherTag) {
        return matchesName(otherTag) && matchesGroup(otherTag);
    }

    private boolean matchesGroup(PluginTag otherTag) {
        return this.getGroup().isEmpty() || this.getGroup().equals(otherTag.getGroup());
    }

    private boolean matchesName(PluginTag otherTag) {
        return this.getName().equals(otherTag.getName());
    }


    /**
     * Build standard string representation of plugin tag
     * {@code group.name[version]}. Both group and version could be empty.
     *
     * @return
     */
    public String toString() {
        String theGroup = getGroup();
        if (!theGroup.isEmpty()) {
            theGroup = theGroup + ":";
        }

        return theGroup + getName();
    }
}
