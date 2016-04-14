package com.hotpads.databean;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.type.NullableType;
import org.hibernate.type.TypeFactory;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  * Implements a generic enum user type identified / represented by a single identifier / column.
 * <p><ul>
 *    <li>The enum type being represented by the certain user type must be set
 *        by using the 'enumClass' property.</li>
 *    <li>The identifier representing a enum value is retrieved by the identifierMethod.
 *        The name of the identifier method can be specified by the
 *        'identifierMethod' property and by default the name() method is used.</li>
 *    <li>The identifier type is automatically determined by
 *        the return-type of the identifierMethod.</li>
 *    <li>The valueOfMethod is the name of the static factory method returning
 *        the enumeration object being represented by the given indentifier.
 *        The valueOfMethod's name can be specified by setting the 'valueOfMethod'
 *        property. The default valueOfMethod's name is 'valueOf'.</li>
 * </p>
 * <p>
 * Example of an enum type represented by an int value:
 * <code>
 * public enum SimpleNumber {
 *   Unknown(-1), Zero(0), One(1), Two(2), Three(3);
 *   int value;
 *   private SimpleNumber(int value) {
 *       this.value = value;
 *       }
 *
 *   public int toInt() { return value; }
 *   public SimpleNumber fromInt(int value) {
 *         switch(value) {
 *          case 0: return Zero;
 *         case 1: return One;
 *         case 2: return Two;
 *         case 3: return Three;
 *         default:
 *                 return Unknown;
 *     }
 *   }
 * }
 * </code>
 * <p>
 * The Mapping would look like this:
 * <code>
 *    <typedef name="SimpleNumber" class="GenericEnumUserType">
 *        <param name="enumClass">SimpleNumber</param>
 *        <param name="identifierMethod">toInt</param>
 *        <param name="valueOfMethod">fromInt</param>
 *    </typedef>
 *    <class ...>
 *      ...
 *     <property name="number" column="number" type="SimpleNumber"/>
 *    </class>
 * </code>
 *
 * @author Martin Kersten
 * @since 05.05.2005
 */

public class GenericEnumUserType implements UserType, ParameterizedType {
	protected final Logger logger = LoggerFactory.getLogger(getClass());


    private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "name";
    private static final String DEFAULT_VALUE_OF_METHOD_NAME = "valueOf";

    private Class<? extends Enum> enumClass;
    private Class<?> identifierType;
    private Method identifierMethod;
    private Method valueOfMethod;
    private NullableType type;
    private int[] sqlTypes;

    @Override
	public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClass");
        try {
            enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
        } catch (ClassNotFoundException cfne) {
            throw new HibernateException("Enum class not found", cfne);
        }

        String identifierMethodName = parameters.getProperty("identifierMethod", DEFAULT_IDENTIFIER_METHOD_NAME);

        try {
            identifierMethod = enumClass.getMethod(identifierMethodName, new Class[0]);
            identifierType = identifierMethod.getReturnType();
        } catch (Exception e) {
            throw new HibernateException("Failed to obtain identifier method", e);
        }

        type = (NullableType) TypeFactory.basic(identifierType.getName());

        if (type == null)
            throw new HibernateException("Unsupported identifier type " + identifierType.getName());

        sqlTypes = new int[] { type.sqlType() };

        String valueOfMethodName = parameters.getProperty("valueOfMethod", DEFAULT_VALUE_OF_METHOD_NAME);

        try {
            valueOfMethod = enumClass.getMethod(valueOfMethodName, new Class[] { identifierType });
        } catch (Exception e) {
            throw new HibernateException("Failed to obtain valueOf method", e);
        }
    }

    @Override
	public Class returnedClass() {
        return enumClass;
    }

    @Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        Object identifier = type.get(rs, names[0]);
        if (identifier == null) {
            return null;
        }

        try {
            return valueOfMethod.invoke(enumClass, new Object[] { identifier });
        } catch (Exception e) {
            throw new HibernateException("Exception while invoking valueOf method '" + valueOfMethod.getName() + "' of " +
                    "enumeration class '" + enumClass + "'", e);
        }
    }

    @Override
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        //logger.debug(value.getClass() + "=" + value);
    	try {
            if (value == null) {
                st.setNull(index, type.sqlType());
            } else {
            	if(value.getClass().equals(enumClass)){
            		Object identifier = identifierMethod.invoke(value, null);
            		st.setObject(index, identifier);
            	}else{
            		Object enumInstance = valueOfMethod.invoke(enumClass, new Object[]{value});
            		Object identifier = identifierMethod.invoke(enumInstance, null);
            		st.setObject(index, identifier);
            	}
            }
        } catch (Exception e) {
            throw new HibernateException("Exception while invoking identifierMethod '" + identifierMethod.getName() + "' of " +
                    "enumeration class '" + enumClass + "'", e);
        }
    }

    @Override
	public int[] sqlTypes() {
        return sqlTypes;
    }

    @Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
	public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
	public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
	public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
	public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
	public boolean isMutable() {
        return false;
    }

    @Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }


    public static void main(String[] args) throws Exception{
    	//String enumClassName = "com.hotpads.databean.billing.PropertyImpression$PropertyImpressionValidity";
    	//Class enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
    	//Method identifierMethod = enumClass.getMethod("toInteger", new Class[0]);
//    	PropertyImpressionValidity validity = PropertyImpressionValidity.INVALID_EMPLOYEE;
//    	Method identifierMethod = validity.getClass().getMethod("toInteger", new Class[0]);
//    	Object result = identifierMethod.invoke(validity, null);
//    	System.out.println(result);
    }

}
