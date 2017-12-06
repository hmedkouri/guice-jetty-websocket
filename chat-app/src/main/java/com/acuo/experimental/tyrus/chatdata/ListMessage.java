/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.acuo.experimental.tyrus.chatdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

abstract class ListMessage extends ChatMessage {
    List dataList = new ArrayList();

    ListMessage(String type) {
        super(type);
    }

    void parseDataString(String dataString) {
        StringTokenizer st = new StringTokenizer(dataString, SEP);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (!"".equals(s)) {
                dataList.add(s);
            }
        }
    }

    ListMessage(String type, List dataList) {
        super(type);
        this.dataList = dataList;
    }

    ListMessage(String type, Set dataSet) {
        this(type, new ArrayList(dataSet));
    }

    ListMessage(String type, String elt1, String elt2) {
        this(type, new ArrayList());
        dataList.add(elt1);
        dataList.add(elt2);
    }

    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder(type);

        for (Iterator itr = dataList.iterator(); itr.hasNext(); ) {
            builder.append(SEP);
            builder.append(itr.next());
        }

        return builder.toString();
    }

    @Override
    List getData() {
        return dataList;
    }
}