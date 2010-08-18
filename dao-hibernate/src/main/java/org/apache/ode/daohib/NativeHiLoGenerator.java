/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.daohib;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.SequenceHiLoGenerator;
import org.hibernate.id.TableHiLoGenerator;
import org.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NativeHiLoGenerator implements IdentifierGenerator, PersistentIdentifierGenerator, Configurable {
    private static final Log __log = LogFactory.getLog(NativeHiLoGenerator.class);
    private IdentifierGenerator _proxy;

    public NativeHiLoGenerator() {
        super();
    }

    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        return _proxy.generate(session, object);
    }

    public Object generatorKey() {
        if (_proxy instanceof PersistentIdentifierGenerator)
            return ((PersistentIdentifierGenerator) _proxy).generatorKey();
        else
            return this;
    }

    public String[] sqlCreateStrings(Dialect dialect) throws HibernateException {
        if (_proxy instanceof PersistentIdentifierGenerator)
            return ((PersistentIdentifierGenerator) _proxy).sqlCreateStrings(dialect);
        else
            return new String[] {};
    }

    public String[] sqlDropStrings(Dialect dialect) throws HibernateException {
        if (_proxy instanceof PersistentIdentifierGenerator)
            return ((PersistentIdentifierGenerator) _proxy).sqlDropStrings(dialect);
        else
            return null;
    }

    public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
        Class generatorClass = null;
        if (dialect.supportsSequences()) {
            __log.debug("Using SequenceHiLoGenerator");
            generatorClass = SequenceHiLoGenerator.class;
        } else {
            generatorClass = TableHiLoGenerator.class;
            __log.debug("Using native dialect generator " + generatorClass);
        }

        IdentifierGenerator g = null;
        try {
            g = (IdentifierGenerator) generatorClass.newInstance();
        } catch (Exception e) {
            throw new MappingException("", e);
        }

        if (g instanceof Configurable)
            ((Configurable) g).configure(type, params, dialect);

        this._proxy = g;
    }
}
