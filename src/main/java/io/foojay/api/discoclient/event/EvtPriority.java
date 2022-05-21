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

import static eu.hansolo.jdktools.Constants.COLON;
import static eu.hansolo.jdktools.Constants.COMMA;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_CLOSE;
import static eu.hansolo.jdktools.Constants.CURLY_BRACKET_OPEN;
import static eu.hansolo.jdktools.Constants.QUOTES;


public enum EvtPriority {
    LOW(0), NORMAL(1), HIGH(2);

    private int value;


    // ******************** Constructor ***************************************
    EvtPriority(final int value) {
        this.value = value;
    }


    // ******************** Methods *******************************************
    public int getValue() { return value; }

    @Override public String toString() {
        return new StringBuilder().append(CURLY_BRACKET_OPEN)
                                  .append(QUOTES).append("class").append(QUOTES).append(COLON).append(QUOTES).append(getClass().getName()).append(QUOTES).append(COMMA)
                                  .append(QUOTES).append("value").append(QUOTES).append(COLON).append(getValue())
                                  .append(CURLY_BRACKET_CLOSE)
                                  .toString();
    }
}
