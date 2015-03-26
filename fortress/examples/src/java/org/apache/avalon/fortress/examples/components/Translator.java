/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.fortress.examples.components;

/**
 * Translator component. This component provides simple translations of given
 * keys, identified by language name.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.6 $ $Date: 2004/02/24 22:31:21 $
 */
public interface Translator
{
    /**
     * Role identifying Component
     */
    String ROLE = Translator.class.getName();

    /**
     * <code>getSupportedLanguages</code> returns an array of String
     * objects detailing which languages are supported for the given
     * key.
     *
     * @param key a <code>String</code> value identifying a translation
     * @return a <code>String[]</code> array containing available language
     * translations for the given key
     */
    String[] getSupportedLanguages( String key );

    /**
     * <code>getTranslation</code> obtains a translation for a given
     * key in a given language. The language parameter must be listed
     * in <code>getSupportedLanguages</code>.
     *
     * @param key a <code>String</code> value identifying a translation
     * @param language a <code>String</code> value identifying the language
     * @return translated text
     */
    String getTranslation( String key, String language );
}

