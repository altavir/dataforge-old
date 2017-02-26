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
package hep.dataforge.storage.commons;

/**
 *
 * @author Alexander Nozik
 */
public class EnvelopeCodes {

    //Enevelope codes

    /**
     * Leadingsymbols
     */
    public static final int DATAFORGE_ENVELOPE = 0x44460000;//DFxx

    /**
     * A DataForge system message type
     */
    public static final short DATAFORGE_MESSAGE_ENVELOPE_CODE = 0x4d45;//ME

    /**
     * A DataForge permanent storage type
     */
    public static final short DATAFORGE_STORAGE_ENVELOPE_CODE = 0x5354;//ST

    //Storage loader data types
    public static final int LOADER_TYPE_CODE = 0x00004C4F;//xxLO
    public static final int POINT_LOADER_TYPE_CODE = LOADER_TYPE_CODE | 0x504f4C4F;//POLO
    public static final int EVENT_LOADER_TYPE_CODE = LOADER_TYPE_CODE | 0x45564C4F;//EVLO
    public static final int STATE_LOADER_TYPE_CODE = LOADER_TYPE_CODE | 0x53544C4F;//STLO
    public static final int OBJECT_LOADER_TYPE_CODE = LOADER_TYPE_CODE | 0x4F424C4f;//OBLO   	
    public static final int ENVELOPE_LOADER_TYPE_CODE = LOADER_TYPE_CODE | 0x454e4c4f;//ENLO   	

    
    //Message data types
    public static final int MESSAGE_TERMINATOR_CODE = 0xffffffff;// The code to terminate connection

    public static final int WRAPPER_TYPE_CODE = 0x57524150;//WRAP - message is a wrapper for another envelope

    public static final int MESSAGE_REQUEST_CODE = 0x52510000;// RQxx - request
    public static final int MESSAGE_CONFIRM_CODE = 0x4f4b0000;// OKxx - operation sucessfull

    public static final int MESSAGE_HAS_META_FLAG = 0x00002b00;// xx+x - message has informative meta
    public static final int MESSAGE_HAS_DATA_FLAG = 0x0000002b;// xxx+ - message has binary data

    public static final int MESSAGE_FAIL_CODE = 0x4641494c;// FAIL - operation unsuccessfull
}
