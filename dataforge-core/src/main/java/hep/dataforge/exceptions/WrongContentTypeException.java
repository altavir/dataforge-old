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
package hep.dataforge.exceptions;

/**
 * <p>WrongContentTypeException class.</p>
 *
 * @author Alexander Nozik
 * @version $Id: $Id
 */
public class WrongContentTypeException extends ContentException {

    private Class expectedType;
    private Class type;

    /**
     * <p>Constructor for WrongContentTypeException.</p>
     *
     * @param expectedType a {@link java.lang.Class} object.
     * @param foundType a {@link java.lang.Class} object.
     */
    public WrongContentTypeException(Class expectedType, Class foundType) {
        this.expectedType = expectedType;
        this.type = foundType;
    }

    /**
     * <p>Constructor for WrongContentTypeException.</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public WrongContentTypeException(String msg) {
        super(msg);
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        if (expectedType != null && type != null) {
            return String.format("Expected %s but found %s", expectedType.getName(), type.getName());
        } else {
            return super.getMessage();
        }
    }

}
