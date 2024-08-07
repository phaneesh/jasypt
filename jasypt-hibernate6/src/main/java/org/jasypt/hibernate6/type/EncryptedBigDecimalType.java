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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jasypt.encryption.pbe.PBEBigDecimalEncryptor;
import org.jasypt.encryption.pbe.StandardPBEBigDecimalEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.hibernate6.encryptor.HibernatePBEBigDecimalEncryptor;
import org.jasypt.hibernate6.encryptor.HibernatePBEEncryptorRegistry;

/**
 * <p>
 * A <b>Hibernate</b> <tt>UserType</tt> implementation which allows transparent 
 * encryption of BigDecimal values during persistence of entities.
 * </p>
 * <p>
 * <i>This class is intended only for declarative use from a Hibernate mapping
 * file. Do not use it directly from your <tt>.java</tt> files (although
 * of course you can use it when mapping entities using annotations).</i>
 * </p>
 * <p>
 * To use this Hibernate type in one of your Hibernate mappings, you can
 * add it like this:
 * </p>
 * <p>
 * <pre>
 *  &lt;hibernate-mapping package="myapp">
 *    ...
 *    &lt;typedef name="<b>encryptedBigDecimal</b>" class="org.jasypt.hibernate.type.EncryptedBigDecimalType">
 *      &lt;param name="encryptorRegisteredName"><b><i>myHibernateBigDecimalEncryptor</i></b>&lt;/param>
 *      &lt;param name="decimalScale"><b><i>2</i></b>&lt;/param>
 *    &lt;/typedef>
 *    ...
 *    &lt;class name="UserData" table="USER_DATA">
 *      ...
 *      &lt;property name="salary" column="SALARY" type="<b>encryptedBigDecimal</b>" />
 *      ...
 *    &lt;class>
 *    ...
 *  &lt;hibernate-mapping>
 * </pre>
 * </p>
 * <p>
 * ...where a <tt>HibernatePBEBigDecimalEncryptor</tt> object
 * should have been previously registered to be used
 * from Hibernate with name <tt>myHibernateBigDecimalEncryptor</tt> (see
 * {@link HibernatePBEBigDecimalEncryptor} and {@link HibernatePBEEncryptorRegistry}). 
 * </p>
 * <p>
 * Or, if you prefer to avoid registration of encryptors, you can configure
 * your encryptor directly in the mapping file (although not recommended), 
 * like this:
 * </p>
 * <p>
 * <pre>
 *  &lt;hibernate-mapping package="myapp">
 *    ...
 *    &lt;typedef name="<b>encryptedBigDecimal</b>" class="org.jasypt.hibernate.type.EncryptedBigDecimalType">
 *      &lt;param name="algorithm"><b><i>PBEWithMD5AndTripleDES</i></b>&lt;/param>
 *      &lt;param name="password"><b><i>XXXXX</i></b>&lt;/param>
 *      &lt;param name="keyObtentionIterations"><b><i>1000</i></b>&lt;/param>
 *      &lt;param name="decimalScale"><b><i>2</i></b>&lt;/param>
 *    &lt;/typedef>
 *    ...
 *    &lt;class name="UserData" table="USER_DATA">
 *      ...
 *      &lt;property name="address" column="ADDRESS" type="<b>encryptedBigDecimal</b>" />
 *      ...
 *    &lt;class>
 *    ...
 *  &lt;hibernate-mapping>
 * </pre>
 * </p>
 * <p>
 * </p>
 * <b>About the <tt>decimalScale</tt> parameter</b>
 * <p>
 * The <tt>decimalScale</tt> parameter is aimed at setting the scale with which
 * BigDecimal numbers will be set to and retrieved from the database. It is
 * an important parameter because many DBMSs return BigDecimal numbers with
 * a scale equal to the amount of decimal positions declared for the field
 * (e.g. if we store "18.23" (scale=2) in a DECIMAL(15,5) field, we can get a 
 * "18.23000" (scale=5) back when we retrieve the number). This can affect
 * correct decryption of encrypted numbers, but specifying a 
 * <tt>decimalScale</tt> parameter will solve this issue.
 * </p>
 * <p>
 * So, if we set <tt>decimalScale</tt> to 3, and we store "18.23", this 
 * Hibernate type will send "18.230" to the encryptor, which is the value that
 * we will get back from the database at retrieval time (a scale of "3" 
 * will be set again on the value obtained from DB). If it is necessary, a 
 * <i>DOWN</i> rounding operation is executed on the number. 
 * </p>
 * <hr/>
 * <p>
 * To learn more about usage of user-defined types, please refer to the
 * <a href="http://www.hibernate.org" target="_blank">Hibernate Reference
 * Documentation</a>.
 * </p>
 * 
 * 
 * @since 1.9.0
 * 
 * @author Chus Picos
 * 
 */
public final class EncryptedBigDecimalType implements UserType, ParameterizedType {

    private static final int sqlType = Types.NUMERIC;
    private static final int[] sqlTypes = new int[]{ sqlType };
    
    private boolean initialized = false;
    private boolean useEncryptorName = false;
    
    private String encryptorName = null;
    private String algorithm = null;
    private String password = null;
    private Integer keyObtentionIterations = null;
    private Integer decimalScale = null;
    
    private PBEBigDecimalEncryptor encryptor = null;

    
    public int[] sqlTypes() {
        return sqlTypes.clone();
    }

    
    public Class returnedClass() {
        return BigDecimal.class;
    }

    
    public boolean equals(final Object x, final Object y)
            throws HibernateException {
        return x == y || (x != null && x.equals(y));
    }
    
    
    public Object deepCopy(final Object value)
            throws HibernateException {
        return value;
    }
    
    
    public Object assemble(final Serializable cached, final Object owner)
            throws HibernateException {
        if (cached == null) {
            return null;
        }
        return deepCopy(cached);
    }

    
    public Serializable disassemble(final Object value) 
            throws HibernateException {
        if (value == null) {
            return null;
        }
        return (Serializable) deepCopy(value);
    }

    
    public boolean isMutable() {
        return false;
    }


    public int hashCode(final Object x)
            throws HibernateException {
        return x.hashCode();
    }

    
    public Object replace(final Object original, final Object target, final Object owner) 
            throws HibernateException {
        return original;
    }

    
    public Object nullSafeGet(final ResultSet rs, final String[] names,
            final SharedSessionContractImplementor session, final Object owner)
            throws HibernateException, SQLException {
        checkInitialization();
        final BigDecimal storedEncryptedMessage = rs.getBigDecimal(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        final BigDecimal scaledEncryptedMessage = 
            storedEncryptedMessage.setScale(
                    this.decimalScale.intValue(), RoundingMode.UNNECESSARY);
        return this.encryptor.decrypt(scaledEncryptedMessage);
    }

    
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
            final SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        checkInitialization();
        if (value == null) {
            st.setNull(index, sqlType);
        } else {
            final BigDecimal scaledValue = 
                ((BigDecimal) value).setScale(
                        this.decimalScale.intValue(), RoundingMode.DOWN);
            final BigDecimal encryptedMessage = 
                this.encryptor.encrypt(scaledValue);
            st.setBigDecimal(index, encryptedMessage);
        }
    }

    
    public synchronized void setParameterValues(final Properties parameters) {
        
        final String paramEncryptorName =
            parameters.getProperty(ParameterNaming.ENCRYPTOR_NAME);
        final String paramAlgorithm =
            parameters.getProperty(ParameterNaming.ALGORITHM);
        final String paramPassword =
            parameters.getProperty(ParameterNaming.PASSWORD);
        final String paramKeyObtentionIterations =
            parameters.getProperty(ParameterNaming.KEY_OBTENTION_ITERATIONS);
        final String paramDecimalScale =
            parameters.getProperty(ParameterNaming.DECIMAL_SCALE);
        
        this.useEncryptorName = false;
        if (paramEncryptorName != null) {
            
            if ((paramAlgorithm != null) ||
                (paramPassword != null) ||
                (paramKeyObtentionIterations != null)) {
                
                throw new EncryptionInitializationException(
                        "If \"" + ParameterNaming.ENCRYPTOR_NAME + 
                        "\" is specified, none of \"" +
                        ParameterNaming.ALGORITHM + "\", \"" +
                        ParameterNaming.PASSWORD + "\" or \"" + 
                        ParameterNaming.KEY_OBTENTION_ITERATIONS + "\" " +
                        "can be specified");
                
            }
            this.encryptorName = paramEncryptorName;
            this.useEncryptorName = true;
            
        } else if ((paramPassword != null)) {

            this.password = paramPassword;
            
            if (paramAlgorithm != null) {
                this.algorithm = paramAlgorithm;
            }
            
            if (paramKeyObtentionIterations != null) {

                try {
                    this.keyObtentionIterations =
                        Integer.valueOf(Integer.parseInt(paramKeyObtentionIterations));
                } catch (NumberFormatException e) {
                    throw new EncryptionInitializationException(
                            "Value specified for \"" + 
                            ParameterNaming.KEY_OBTENTION_ITERATIONS + 
                            "\" is not a valid integer");
                }
                
            }
            
        } else {
            
            throw new EncryptionInitializationException(
                    "If \"" + ParameterNaming.ENCRYPTOR_NAME + 
                    "\" is not specified, then \"" +
                    ParameterNaming.PASSWORD + "\" (and optionally \"" +
                    ParameterNaming.ALGORITHM + "\" and \"" + 
                    ParameterNaming.KEY_OBTENTION_ITERATIONS + "\") " +
                    "must be specified");
            
        }
        
        if (paramDecimalScale != null) {
            
            try {
                this.decimalScale =
                    Integer.valueOf(Integer.parseInt(paramDecimalScale));
            } catch (NumberFormatException e) {
                throw new EncryptionInitializationException(
                        "Value specified for \"" + 
                        ParameterNaming.DECIMAL_SCALE + 
                        "\" is not a valid integer");
            }
            
        } else {
            
            throw new EncryptionInitializationException(
                    ParameterNaming.DECIMAL_SCALE + 
                    " must be specified");
            
        }
        
    }

    
    
    private synchronized void checkInitialization() {
        
        if (!this.initialized) {
            
            if (this.useEncryptorName) {

                final HibernatePBEEncryptorRegistry registry = 
                    HibernatePBEEncryptorRegistry.getInstance();
                final PBEBigDecimalEncryptor pbeEncryptor = 
                    registry.getPBEBigDecimalEncryptor(this.encryptorName);
                if (pbeEncryptor == null) {
                    throw new EncryptionInitializationException(
                            "No big decimal encryptor registered for hibernate " +
                            "with name \"" + this.encryptorName + "\"");
                }
                this.encryptor = pbeEncryptor;
                
            } else {
                
                final StandardPBEBigDecimalEncryptor newEncryptor = 
                    new StandardPBEBigDecimalEncryptor();
                
                newEncryptor.setPassword(this.password);
                
                if (this.algorithm != null) {
                    newEncryptor.setAlgorithm(this.algorithm);
                }
                
                if (this.keyObtentionIterations != null) {
                    newEncryptor.setKeyObtentionIterations(
                            this.keyObtentionIterations.intValue());
                }
                
                newEncryptor.initialize();
                
                this.encryptor = newEncryptor;
                
            }
            
            this.initialized = true;
        }
        
    }

    @Override
    public int getSqlType() {
        return Types.NUMERIC;
    }

    @Override
    public Object nullSafeGet(
        ResultSet resultSet,
        int i,
        SharedSessionContractImplementor sharedSessionContractImplementor,
        Object o)
        throws SQLException {
        checkInitialization();
        final BigDecimal storedEncryptedMessage = resultSet.getBigDecimal(i);
        if (resultSet.wasNull()) {
            return null;
        }
        final BigDecimal scaledEncryptedMessage =
            storedEncryptedMessage.setScale(
                this.decimalScale, RoundingMode.UNNECESSARY);
        return this.encryptor.decrypt(scaledEncryptedMessage);
    }

}
