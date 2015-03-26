/* 
 * Copyright 2003-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avalon.fortress.impl.factory;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 *  <code>BCELCodeGenerator</code> creates implementations for the
 *  {@link org.apache.bcel.classfile.Method Method}s and
 *  {@link org.apache.bcel.classfile.Field Field}s needed in creating a
 *  <code>WrapperClass</code>.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */

public final class BCELCodeGenerator
{
    //***************************************************************************
    // Fields
    //***************************************************************************

    /**
     * The name of the field holding the wrapped class in the generated
     * wrapper, e.g.
     * <pre>
     *  <code>
     *      private final <ClassToWrap> WRAPPED_CLASS_FN;
     *  </code>
     * </pre>
     */
    private static final String WRAPPED_CLASS_FN = "m_wrappedClass";

    /**
     * The name of the field accessor used to ask the generated wrapper for
     * the wrapped class instance.
     */
    private static final String ACCESSOR_METHOD_NAME = "getWrappedObject";

    /**
     * The name of the wrapper class to be created.
     */
    private String m_wrapperClassName;

    /**
     * The name of the superclass of the wrapper class to be generated.
     */
    private String m_wrapperSuperclassName;

    /**
     * Class object holding the type of the object we want to create a
     * wrapper for.
     */
    private JavaClass m_classToWrap;

    /**
     * The {@link org.apache.bcel.generic.Type Type} of the class we want to
     * create a wrapper for.
     */
    private Type m_classToWrapType;

    /**
     * The {@link org.apache.bcel.generic.ClassGen ClassGen} instance to use for
     * code generation.
     */
    private ClassGen m_classGenerator;

    /**
     * The {@link org.apache.bcel.generic.ConstantPoolGen ConstantPoolGen}
     * instance to use for code generation.
     */
    private ConstantPoolGen m_constPoolGenerator;

    /**
     * The {@link org.apache.bcel.generic.InstructionList InstructionList} instance
     * to use during code generation.
     */
    private final InstructionList m_instructionList = new InstructionList();

    /**
     * The {@link org.apache.bcel.generic.InstructionFactory InstructionFactory} to
     * use during code gereration.
     */
    private InstructionFactory m_instructionFactory;

    /**
     * Flag indicating whether this instance is already initialized or not.
     */
    private boolean m_isInitialized = false;

    /**
     * Default constructor.
     */
    public BCELCodeGenerator()
    {
        // Left blank
    }

    public void init(
        final String wrapperClassName,
        final String wrapperSuperclassName,
        final JavaClass classToWrap,
        final ClassGen classGenerator )
        throws IllegalArgumentException
    {
        if ( classToWrap == null )
        {
            final String message = "Target class must not be <null>.";
            throw new IllegalArgumentException( message );
        }
        if ( classToWrap.isAbstract() || !classToWrap.isClass() )
        {
            final String message =
                "Target class must neither be abstract nor an interface.";
            throw new IllegalArgumentException( message );
        }
        if ( classGenerator == null )
        {
            final String message = "ClassGenerator must not be <null>.";
            throw new IllegalArgumentException( message );
        }

        m_wrapperClassName = wrapperClassName;
        m_wrapperSuperclassName = wrapperSuperclassName;
        m_classToWrap = classToWrap;
        m_classToWrapType = new ObjectType( m_classToWrap.getClassName() );
        m_classGenerator = classGenerator;
        m_constPoolGenerator = m_classGenerator.getConstantPool();
        m_instructionFactory =
            new InstructionFactory( m_classGenerator, m_constPoolGenerator );

        m_isInitialized = true;
    }

    /**
     * Create a field declaration of the form
     * <pre>
     *  <code>
     *      private <ClassToWrap> WRAPPED_CLASS_FN;
     *  </code>
     * </pre>
     *
     * @return Field
     *
     * @throws IllegalStateException If this instance is not initialized.
     */
    public Field createWrappedClassField() throws IllegalStateException
    {
        if ( !isInitialized() )
        {
            final String message =
                "BCELMethodFieldImplementationGenerator is not initialized.";
            throw new IllegalStateException( message );
        }

        final FieldGen fg =
            new FieldGen(
                Constants.ACC_PRIVATE,
                m_classToWrapType,
                WRAPPED_CLASS_FN,
                m_constPoolGenerator );

        return fg.getField();
    }

    /**
     * Create the wrapper class' default constructor:
     * <pre>
     *  <code>
     *      public <wrapperClass>(<classToWrap> classToWrap)
     *      {
     *          this.<WRAPPED_CLASS_FN> = classToWrap;
     *      }
     *  </code>
     * </pre>
     *
     * @return The created default constructor
     *
     * @throws IllegalStateException If this instance is not initialized.
     */
    public Method createDefaultConstructor() throws IllegalStateException
    {
        if ( !isInitialized() )
        {
            final String message =
                "BCELMethodFieldImplementationGenerator is not initialized.";
            throw new IllegalStateException( message );
        }
        final MethodGen mg =
            new MethodGen(
                Constants.ACC_PUBLIC,
                Type.VOID,
                new Type[]{m_classToWrapType},
                new String[]{"classToWrap"},
                "<init>",
                m_wrapperClassName,
                m_instructionList,
                m_constPoolGenerator );

        m_instructionList.append(
                InstructionFactory.createLoad( Type.OBJECT, 0 ) );
        m_instructionList.append(
            m_instructionFactory.createInvoke(
                m_wrapperSuperclassName,
                "<init>",
                Type.VOID,
                Type.NO_ARGS,
                Constants.INVOKESPECIAL ) );
        m_instructionList.append(
                InstructionFactory.createLoad( Type.OBJECT, 0 ) );
        m_instructionList.append(
                InstructionFactory.createLoad( Type.OBJECT, 1 ) );
        m_instructionList.append(
            m_instructionFactory.createFieldAccess(
                m_wrapperClassName,
                WRAPPED_CLASS_FN,
                m_classToWrapType,
                Constants.PUTFIELD ) );
        m_instructionList.append( InstructionFactory.createReturn( Type.VOID ) );
        mg.setMaxStack();
        mg.setMaxLocals();

        return extractMethod( mg );
    }

    /**
     * Create a field accessor for the wrapped class instance of the form
     * <pre>
     *  <code>
     *      public Object <ACCESSOR_METHOD_NAME>()
     *      {
     *          return this.<WRAPPED_CLASS_FN>;
     *      }
     *  </code>
     * </pre>
     * @return Method
     * @throws IllegalStateException
     */
    public Method createWrappedClassAccessor() throws IllegalStateException
    {
        if ( !isInitialized() )
        {
            final String message =
                "BCELMethodFieldImplementationGenerator is not initialized.";
            throw new IllegalStateException( message );
        }

        final MethodGen mg =
            new MethodGen(
                Constants.ACC_PUBLIC,
                Type.OBJECT,
                Type.NO_ARGS,
                new String[]{
                },
                ACCESSOR_METHOD_NAME,
                m_classToWrap.getClassName(),
                m_instructionList,
                m_constPoolGenerator );

        m_instructionList.append(
                InstructionFactory.createLoad( Type.OBJECT, 0 ) );
        m_instructionList.append(
            m_instructionFactory.createFieldAccess(
                m_wrapperClassName,
                WRAPPED_CLASS_FN,
                m_classToWrapType,
                Constants.GETFIELD ) );
        m_instructionList.append(
                InstructionFactory.createReturn( Type.OBJECT ) );

        mg.setMaxStack();
        mg.setMaxLocals();

        return extractMethod( mg );
    }

    /**
     * Create a method declaration/definition of the form
     * <pre>
     *  <code>
     *      public <returnType> <methodName>(<parameterTypes>)
     *          throws <exceptionNames>
     *      {
     *          return this.<WRAPPED_CLASS_FN>.<methodName>(<parameterTypes>);
     *      }
     *  </code>
     * </pre>
     *
     * @param meth           The method descriptor
     *
     * @return Method         The {@link org.apache.bcel.classfile.Method Method}
     *                        object representing the created method
     *
     * @throws IllegalArgumentException If any of the parameters passed in is null.
     * @throws IllegalStateException If this instance is not initialized.
     */
    public Method createMethodWrapper( MethodDesc meth )
        throws IllegalArgumentException, IllegalStateException
    {
        if ( !isInitialized() )
        {
            final String message =
                "BCELMethodFieldImplementationGenerator is not initialized.";
            throw new IllegalStateException( message );
        }
        if ( meth.name == null
            || meth.returnType == null
            || meth.parameterTypes == null
            || meth.exceptionNames == null )
        {
            final String message = "None of the parameters may be <null>.";
            throw new IllegalArgumentException( message );
        }

        final MethodGen mg =
            new MethodGen(
                Constants.ACC_PUBLIC,
                meth.returnType,
                meth.parameterTypes,
                null,
                meth.name,
                m_wrapperClassName,
                m_instructionList,
                m_constPoolGenerator );

        // Create throws clause
        for ( int i = 0; i < meth.exceptionNames.length; i++ )
        {
            mg.addException( meth.exceptionNames[i] );
        }

        // Loading the wrapped class instance onto the stack ...
        m_instructionList.append(
                InstructionFactory.createLoad( Type.OBJECT, 0 ) );
        m_instructionList.append(
            m_instructionFactory.createFieldAccess(
                m_wrapperClassName,
                WRAPPED_CLASS_FN,
                m_classToWrapType,
                Constants.GETFIELD ) );

        // Loading all parameters onto the stack ...
        short stackIndex = 1;
        // Stack index 0 is occupied by the wrapped class instance.
        for ( int i = 0; i < meth.parameterTypes.length; ++i )
        {
            m_instructionList.append(
                    InstructionFactory.createLoad( meth.parameterTypes[i], stackIndex ) );
            stackIndex += meth.parameterTypes[i].getSize();
        }

        findImplementation( meth );

        // Invoking the specified method with the loaded parameters on
        // the wrapped class instance ...
        m_instructionList.append(
            m_instructionFactory.createInvoke(
                    meth.implementingClassName,
                    meth.name,
                    meth.returnType,
                    meth.parameterTypes,
                    Constants.INVOKEVIRTUAL ) );

        // Creating return statement ...
        m_instructionList.append( InstructionFactory.createReturn( meth.returnType ) );

        mg.setMaxStack();
        mg.setMaxLocals();

        return extractMethod( mg );
    }

    private void findImplementation( MethodDesc meth )
    {
        JavaClass currentClass = m_classToWrap;

        while ( null != currentClass && null == meth.implementingClassName )
        {
            Method[] methList = currentClass.getMethods();

            for (int i = 0; i < methList.length; i++)
            {
                boolean isEqual = methList[i].isPublic() && !methList[i].isAbstract();
                isEqual = isEqual && methList[i].getName().equals(meth.name);
                isEqual = isEqual && methList[i].getReturnType().equals(meth.returnType);
                isEqual = isEqual && methList[i].getArgumentTypes().length == meth.parameterTypes.length;
                Type[] parameterTypes = methList[i].getArgumentTypes();
                for (int j = 0; isEqual && j < parameterTypes.length; j++)
                {
                    isEqual = isEqual && parameterTypes[j].equals(meth.parameterTypes[j]);
                }

                if (isEqual)
                {
                    meth.implementingClassName = currentClass.getClassName();
                    meth.isFinal = methList[i].isFinal();
                }
            }
            try
            {
                currentClass = currentClass.getSuperClass();
            } catch( Exception e )
            {
                // NH: Actually ClassNotFoundException is declared in the current CVS
                //     Head, but then we can't compile against BCEL5.5 which is our
                //     binary dependency, so I am catching Exception to cover both.
                //
                //     I think this will happen if the superclass is not available
                //     in the classloaders, but I have no clue what is the proper action.
                //     I had to introduce this since BCEL decided to break compatibility
                //     and now throws the Exception.
                //     For now, throwing the absurd IllegalStateException as the rest 
                //     of the codebase seems to be constructed around it.
                throw new IllegalStateException("Superclass of java class could not be found: " + currentClass + ", " + e.getMessage() );
            }
        }

        if (null == meth.implementingClassName)
        {
            throw new IllegalStateException("No concrete class for the requested method: " + meth.toString());
        }
    }

    /**
     * Create a method declaration/definition of the form
     * <pre>
     *  <code>
     *      public <returnType> <methodName>(<parameterTypes>)
     *          throws <exceptionNames>
     *      {
     *          return this.<WRAPPED_CLASS_FN>.<methodName>(<parameterTypes>);
     *      }
     *  </code>
     * </pre>
     *
     * @param methodToWrap The <code>Method</code> to create a wrapper for.
     *
     * @return Method       The wrapper method.
     *
     * @throws IllegalArgumentException If <code>methodToWrao</code> is null.
     * @throws IllegalStateException If this instance is not initialized.
     */
    public Method createMethodWrapper( final Method methodToWrap )
        throws IllegalArgumentException, IllegalStateException
    {
        if ( methodToWrap == null )
        {
            final String message = "Method parameter must not be <null>.";
            throw new IllegalArgumentException( message );
        }

        return createMethodWrapper( new MethodDesc( methodToWrap ) );
    }

    /**
     * Creates an implementation for the supplied {@link org.apache.bcel.classfile.JavaClass JavaClass}
     * instance representing an interface.
     *
     * @param interfacesToImplement The interfaces we want to create an implementation for
     * @return Method[]            An array of {@link org.apache.bcel.classfile.Method Method}
     *                              instances representing the interface implementation.
     * @throws IllegalArgumentException If <code>interfaceToImplement</code> is <code>null</code>
     *                                   or does not represent an interface
     * @throws IllegalStateException    If this instance has not been initialized
     */
    public Method[] createImplementation( final JavaClass[] interfacesToImplement )
        throws Exception
    {
        if ( interfacesToImplement == null )
        {
            final String message = "Interface to implement must not be <null>.";
            throw new IllegalArgumentException( message );
        }
        if ( !isInitialized() )
        {
            final String message =
                "BCELInterfaceImplementationGenerator is not initialized.";
            throw new IllegalStateException( message );
        }
        final Set gmList = new HashSet();

        final MethodDesc[] interfaceMethods = extractMethods( interfacesToImplement );
        for ( int i = 0; i < interfaceMethods.length; ++i )
        {
            final MethodDesc im = interfaceMethods[i];

            // Skip <clinit> method ...
            if ( im.name.equals( "<clinit>" ) )
            {
                continue;
            }

            final Method generatedMethod =
                createMethodWrapper( im );

            gmList.add( generatedMethod );
        }

        return (Method[]) gmList.toArray( new Method[gmList.size()] );
    }

    /**
     * Extracts the {@link org.apache.bcel.classfile.Method Method} out of
     * the supplied {@link org.apache.bcel.generic.MethodGen MethodGen} instance,
     * clears the {@link org.apache.bcel.generic.InstructionList InstructionList}
     * and returns the extracted <code>Method</code>.
     *
     * @param mg The {@link org.apache.bcel.generic.MethodGen MethodGen} instance
     *            holding the {@link org.apache.bcel.classfile.Method Method} to
     *            extract
     * @return   The extracted {@link org.apache.bcel.classfile.Method Method}
     */
    private Method extractMethod( final MethodGen mg )
    {
        final Method m = mg.getMethod();
        m_instructionList.dispose();
        return m;
    }

    /**
     * Has this instance already been initialized?
     *
     * @return TRUE, if this instance has already been initialized, FALSE otherwise
     */
    private boolean isInitialized()
    {
        return m_isInitialized;
    }

    /**
     * Extracts the collection of {@link org.apache.bcel.classfile.Method Method}s
     * declared in the supplied {@link org.apache.bcel.classfile.JavaClass JavaClass}
     * instance. This instance is supposed to represent an interface.
     *
     * @param interfacesToImplement The {@link org.apache.bcel.classfile.JavaClass JavaClass}
     *                              instances representing the interfaces we are asking for
     *                              its methods.
     * @return MethodDesc[]         The array of {@link org.apache.bcel.classfile.Method Method}s
     *                              declared by the interface
     * @throws IllegalArgumentException If <code>interfaceToImplement</code> does not represent an interface
     * @throws NullPointerException if the <code>interfaceToImplement</code> is <code>null</code>
     */
    static MethodDesc[] extractMethods( final JavaClass interfacesToImplement[] )
        throws Exception
    {
        if ( interfacesToImplement == null )
        {
            final String message = "JavaClass[] parameter must not be <null>.";
            throw new NullPointerException( message );
        }

        Set methods = new HashSet();
        for (int x = 0; x < interfacesToImplement.length; x++)
        {
            JavaClass iface = interfacesToImplement[x];
            if ( !iface.isInterface() )
            {
                final String message = "JavaClass parameter must be an interface";
                throw new IllegalArgumentException( message );
            }

            extractMethods( iface, methods );
        }

        return (MethodDesc[]) methods.toArray( new MethodDesc[]{} );
    }

    private static final void extractMethods( final JavaClass interfaceToImplement, final Set methods )
    {
        Method[] meth = interfaceToImplement.getMethods();
        for ( int m = 0; m < meth.length; m++ )
        {
            MethodDesc desc = new MethodDesc(meth[m]);
            methods.add( desc );
        }
    }

    private static final class MethodDesc
    {
        final String name;
        final Type returnType;
        final Type[] parameterTypes;
        String[] exceptionNames;
        boolean isFinal;
        String implementingClassName;

        MethodDesc( Method meth )
        {
            this(meth.getName(), meth.getReturnType(), meth.getArgumentTypes(),
                    (null == meth.getExceptionTable() ) ? new String[0] : meth.getExceptionTable().getExceptionNames());
        }

        MethodDesc(String name, Type returnType, Type[] parameterTypes, String[] exceptionNames)
        {
            this.name = name;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
            this.exceptionNames = exceptionNames;
            isFinal = false;
        }

        public boolean equals(Object o)
        {
            MethodDesc other = (MethodDesc)o;
            boolean isEqual = name.equals(other.name);
            isEqual = isEqual && returnType.equals(other.returnType);
            isEqual = isEqual && parameterTypes.length == other.parameterTypes.length;

            for (int i = 0; isEqual && i < parameterTypes.length; i++)
            {
                isEqual = isEqual && parameterTypes[i].equals(other.parameterTypes[i]);
            }

            return isEqual;
        }

        public int hashCode()
        {
            int hash = name.hashCode();
            hash >>>= 5;
            hash ^= returnType.hashCode();

            for (int i = 0; i < parameterTypes.length; i++)
            {
                hash >>>= parameterTypes.length;
                hash ^= parameterTypes[i].hashCode();
            }

            return hash;
        }

        public String toString()
        {
            StringBuffer str = new StringBuffer(returnType.getSignature());
            str.append( " " ).append( name ).append( "(" );

            for(int i = 0; i < parameterTypes.length; i++)
            {
                str.append(parameterTypes[i].toString());
                if (i > 0) str.append(",");
            }

            str.append("),").append((isFinal) ? "f" : "v");

            return str.toString();
        }
    }
}
