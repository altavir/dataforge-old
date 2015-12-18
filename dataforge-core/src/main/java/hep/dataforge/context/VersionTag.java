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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The tag which contains information about name, group and version of some
 * object. It also could contain any complex rule to define version ranges
 *
 * @author Alexander Nozik
 */
public class VersionTag {

    public static final String UNVERSIONED = "";
    public static final String DEFAULT_GROUP = "";

    /**
     * Build new VersionTag from standard string representation
     *
     * @param tag
     * @return
     */
    public static VersionTag fromString(String tag) {
        Pattern pattern = Pattern.compile("(?:(?<group>.*):)?(?<name>[^\\[\\]]*)(?:\\[(?<version>.*)\\])?");
        Matcher matcher = pattern.matcher(tag);
        if (!matcher.matches()) {
            throw new RuntimeException("Can't parse tag from string. Wrong sytax.");
        }
        String group = matcher.group("group");
        String name = matcher.group("name");
        String version = matcher.group("version");
        return new VersionTag(group, name, version);

    }

    private final String name;
    private final String group;
    private final String version;

    public VersionTag(String pluginGroup, String pluginName, String pluginVersion) {
        if (pluginName == null || pluginName.isEmpty()) {
            throw new IllegalStateException("Can create a tag with empty name");
        }
        this.name = pluginName;
        this.group = pluginGroup;
        this.version = pluginVersion;
    }

//    public VersionTag(String pluginName) {
//        this(DEFAULT_GROUP, pluginName, UNVERSIONED);
//    }

    /**
     * Build standard string representation in form of
     * {@code group.name[version]}. Both group and version could be empty.
     *
     * @return
     */
    public String getFullName() {
        String theGroup = group();
        if (!theGroup.equals(DEFAULT_GROUP)) {
            theGroup = theGroup + ":";
        }

        String theVersion = version();

        if (!theVersion.equals(UNVERSIONED)) {
            theVersion = "[" + theVersion + "]";
        }

        return theGroup + name() + theVersion;
    }

    /**
     * @return the pluginName
     */
    public String name() {
        return name;
    }

    /**
     * @return the pluginGroup
     */
    public String group() {
        if (group == null) {
            return DEFAULT_GROUP;
        }
        return group;
    }

    /**
     * @return the pluginVersion
     */
    public String version() {
        if (version == null) {
            return UNVERSIONED;
        }
        return version;
    }

    /**
     * Check if given tag is compatible (in range) of this tag
     *
     * @param otherTag
     * @return
     */
    public boolean matches(VersionTag otherTag) {
        return matchesName(otherTag) && matchesGroup(otherTag) && matchesVersion(otherTag);
    }

    protected boolean matchesGroup(VersionTag otherTag) {
        return this.group().isEmpty() || this.group().equals(otherTag.group());
    }

    protected boolean matchesName(VersionTag otherTag) {
        return this.name().equals(otherTag.name());
    }

    protected boolean matchesVersion(VersionTag otherTag) {
        return this.version().isEmpty() || this.version().equals(otherTag.version());
    }

    @Override
    public int hashCode() {
        return getFullName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof VersionTag) && this.getFullName().equals(((VersionTag) obj).getFullName());
    }

}
