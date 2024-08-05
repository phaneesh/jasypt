/*
 * =============================================================================
 *
 *   Copyright (c) 2007-2010, The JASYPT team (http://www.jasypt.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.jasypt.hibernate6.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.jasypt.hibernate6.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.hibernate6.encryptor.HibernatePBEStringEncryptor;

/**
 * A <b>Hibernate</b> <tt>UserType</tt> implementation which allows encryption of Byte values into
 * String (VARCHAR) database fields during persistence of entities.
 *
 * <p><i>This class is intended only for declarative use from a Hibernate mapping file. Do not use
 * it directly from your <tt>.java</tt> files (although of course you can use it when mapping
 * entities using annotations).</i>
 *
 * <p>To use this Hibernate type in one of your Hibernate mappings, you can add it like this:
 *
 * <p>
 *
 * <pre>
 *  &lt;hibernate-mapping package="myapp">
 *    ...
 *    &lt;typedef name="<b>encryptedByteAsString</b>" class="org.jasypt.hibernate.type.EncryptedByteAsStringType">
 *      &lt;param name="encryptorRegisteredName"><b><i>myHibernateStringEncryptor</i></b>&lt;/param>
 *    &lt;/typedef>
 *    ...
 *    &lt;class name="UserData" table="USER_DATA">
 *      ...
 *      &lt;property name="salary" column="SALARY" type="<b>encryptedByteAsString</b>" />
 *      ...
 *    &lt;class>
 *    ...
 *  &lt;hibernate-mapping>
 * </pre>
 *
 * <p>...where a <tt>HibernatePBEStringEncryptor</tt> object should have been previously registered
 * to be used from Hibernate with name <tt>myHibernateStringEncryptor</tt> (see {@link
 * HibernatePBEStringEncryptor} and {@link HibernatePBEEncryptorRegistry}).
 *
 * <p>Or, if you prefer to avoid registration of encryptors, you can configure your encryptor
 * directly in the mapping file (although not recommended), like this:
 *
 * <p>
 *
 * <pre>
 *  &lt;hibernate-mapping package="myapp">
 *    ...
 *    &lt;typedef name="<b>encryptedByteAsString</b>" class="org.jasypt.hibernate.type.EncryptedByteAsStringType">
 *      &lt;param name="algorithm"><b><i>PBEWithMD5AndTripleDES</i></b>&lt;/param>
 *      &lt;param name="password"><b><i>XXXXX</i></b>&lt;/param>
 *      &lt;param name="keyObtentionIterations"><b><i>1000</i></b>&lt;/param>
 *    &lt;/typedef>
 *    ...
 *    &lt;class name="UserData" table="USER_DATA">
 *      ...
 *      &lt;property name="salary" column="SALARY" type="<b>encryptedByteAsString</b>" />
 *      ...
 *    &lt;class>
 *    ...
 *  &lt;hibernate-mapping>
 * </pre>
 *
 * <p>To learn more about usage of user-defined types, please refer to the <a
 * href="http://www.hibernate.org" target="_blank">Hibernate Reference Documentation</a>.
 *
 * @since 1.9.0
 * @author Chus Picos
 */
public final class EncryptedByteAsStringType extends AbstractEncryptedAsStringType {

  protected Object convertToObject(final String string) {
    return Byte.valueOf(string);
  }

  @Override
  public int getSqlType() {
    return Types.BIT;
  }

  public Class<Byte> returnedClass() {
    return Byte.class;
  }

  @Override
  public Object nullSafeGet(
      ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
      throws SQLException {
    return Objects.nonNull(rs.getString(position)) ? convertToObject(rs.getString(position)) : null;
  }
}
