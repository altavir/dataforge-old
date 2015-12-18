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
 * Ошибка выбрасывается если формат запроса не поддержвивается сервером или если
 * произошла ошибка обработки запроса
 *
 * @author Darksnake
 */
public class StorageQueryException extends StorageException {
    
    String queryType;
    String query;

    public StorageQueryException(String queryType, String query) {
        this.queryType = queryType;
        this.query = query;
    }

    public StorageQueryException(String queryType, String query, Throwable cause) {
        super(cause);
        this.queryType = queryType;
        this.query = query;
    }

}
