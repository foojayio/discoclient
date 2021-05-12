/*
 * Copyright (c) 2021 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.foojay.api.discoclient.event;

public class DCEvt extends Evt {
    public static final EvtType<DCEvt> ANY = new EvtType<>(Evt.ANY, "DC_EVENT");

    public DCEvt(final EvtType<? extends DCEvt> evtType) {
        super(evtType);
    }
    public DCEvt(final Object source, final EvtType<? extends DCEvt> evtType) {
        super(source, evtType);
    }
    public DCEvt(final Object source, final EvtType<? extends DCEvt> evtType, final EvtPriority priority) {
        super(source, evtType, priority);
    }


    @Override public EvtType<? extends DCEvt> getEvtType() {
        return (EvtType<? extends DCEvt>) super.getEvtType();
    }
}
