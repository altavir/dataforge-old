/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.io.messages

import hep.dataforge.io.envelopes.Envelope

/**
 * A Responder that does not respond itself but delegates response to
 * appropriate responder.
 *
 * @author Alexander Nozik
 */
interface ResponseDispatcher : Responder, Dispatcher {

    override fun respond(message: Envelope): Envelope {
        return getResponder(message).respond(message)
    }

}
