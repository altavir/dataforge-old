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

import hep.dataforge.description.ValueDef;
import hep.dataforge.meta.Meta;
import hep.dataforge.meta.MetaBuilder;
import hep.dataforge.names.AlphanumComparator;
import hep.dataforge.utils.SimpleMetaMorph;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The tag which contains information about name, group and version of some
 * object. It also could contain any complex rule to define version ranges
 *
 * @author Alexander Nozik
 */
@ValueDef(name = "name")
@ValueDef(name = "group")
@ValueDef(name = "version")
//@ValueDef(name = "role")
@ValueDef(name = "priority", type = "NUMBER", info = "Plugin load priority. Used for plugins with the same role")
public class PluginTag extends SimpleMetaMorph implements Comparable<PluginTag> {

    /**
     * Build new PluginTag from standard string representation
     *
     * @param tag
     * @return
     */
    public static PluginTag fromString(String tag) {
        Pattern pattern = Pattern.compile("(?:(?<group>.*):)?(?<name>[^\\[\\]]*)(?:\\[(?<version>.*)\\])?");
        Matcher matcher = pattern.matcher(tag);
        if (!matcher.matches()) {
            throw new RuntimeException("Can't parse tag from string. Wrong sytax.");
        }
        String group = matcher.group("group");
        String name = matcher.group("name");
        String version = matcher.group("version");
        return new PluginTag(group, name, version);

    }

    public PluginTag(Meta meta) {
        super(meta);
    }

    public PluginTag() {
    }

    public PluginTag(String group, String name, String version) {
        super(new MetaBuilder("plugin")
                .setValue("group", group)
                .setValue("name", name)
                .setValue("version", version)
        );
    }

//    public String getRole() {
//        return getString("role", "");
//    }


    public String getName() {
        return getString("name", "");
    }

    public String getGroup() {
        return getString("group", "");
    }


    public String getVersion() {
        return getString("version", "");
    }

    /**
     * @return the pluginVersion
     */
    public double getPriority() {
        return getDouble("priority", 0);
    }


    /**
     * Check if given tag is compatible (in range) of this tag
     *
     * @param otherTag
     * @return
     */
    public boolean matches(PluginTag otherTag) {
        return matchesName(otherTag) && matchesGroup(otherTag) && matchesVersion(otherTag);
    }

    private boolean matchesGroup(PluginTag otherTag) {
        return this.getGroup().isEmpty() || this.getGroup().equals(otherTag.getGroup());
    }

    private boolean matchesName(PluginTag otherTag) {
        return this.getName().equals(otherTag.getName());
    }

    private boolean matchesVersion(PluginTag otherTag) {
        //TODO add version diapasons
        return this.getVersion().isEmpty() || this.getVersion().equals(otherTag.getVersion());
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

        String theVersion = getVersion();

        if (!theVersion.isEmpty()) {
            theVersion = "[" + theVersion + "]";
        }

        return theGroup + getName() + theVersion;
    }


    @Override
    public int compareTo(PluginTag o) {
        return Double.compare(this.getPriority(), o.getPriority()) * 10 + AlphanumComparator.INSTANCE.compare(this.getVersion(), o.getVersion());
    }
}
