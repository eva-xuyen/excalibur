/* 
 * Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.excalibur.source.test;

import org.apache.excalibur.source.SourceUtil;

import junit.framework.TestCase;

/**
 * Test case for SourceUtil.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SourceUtilTestCase.java,v 1.4 2004/02/28 11:47:22 cziegeler Exp $
 */
public class SourceUtilTestCase extends TestCase
{
    public SourceUtilTestCase()
    {
        this("SourceUtil");
    }

    public SourceUtilTestCase(String name)
    {
        super(name);
    }

    public void testNominalScheme() throws Exception
    {
        String uri = "http://foo";
        assertEquals(4, SourceUtil.indexOfSchemeColon(uri));
        assertEquals("http", SourceUtil.getScheme(uri));
        assertEquals("//foo", SourceUtil.getSpecificPart(uri));
    }

    public void testDoubleColon() throws Exception
    {
        assertEquals(4, SourceUtil.indexOfSchemeColon("file:foo:bar"));
    }

    public void testSpecialScheme() throws Exception
    {
        String uri = "a-+.:foo"; // Strange, but valid !
        assertEquals(4, SourceUtil.indexOfSchemeColon(uri));
        assertEquals("a-+.", SourceUtil.getScheme(uri));
        assertEquals("foo", SourceUtil.getSpecificPart(uri));
    }

    public void testSpecialPart() throws Exception
    {
        String uri = "bar:";
        assertEquals(3, SourceUtil.indexOfSchemeColon(uri));
        assertEquals("bar", SourceUtil.getScheme(uri));
        assertEquals("", SourceUtil.getSpecificPart(uri));
    }

    public void testInvalidScheme() throws Exception
    {
        String uri = "2foo:bar";
        assertEquals(-1, SourceUtil.indexOfSchemeColon(uri));
        assertEquals(null, SourceUtil.getScheme(uri));
        assertEquals(null, SourceUtil.getSpecificPart(uri));

        // Invalid character before any of the allowed ones
        assertEquals(-1, SourceUtil.indexOfSchemeColon("h ttp:foo"));
        assertEquals(-1, SourceUtil.indexOfSchemeColon(" http:foo"));
        assertEquals(-1, SourceUtil.indexOfSchemeColon("http :foo"));

       // Invalid character between allowed ranges
        assertEquals(-1, SourceUtil.indexOfSchemeColon("h_ttp:foo"));
        assertEquals(-1, SourceUtil.indexOfSchemeColon("_http:foo"));
        assertEquals(-1, SourceUtil.indexOfSchemeColon("http_:foo"));

        // Invalid character after any of the allowed ones
        assertEquals(-1, SourceUtil.indexOfSchemeColon("h~ttp:foo"));
        assertEquals(-1, SourceUtil.indexOfSchemeColon("~http:foo"));
        assertEquals(-1, SourceUtil.indexOfSchemeColon("http~:foo"));
    }

    public void testAbsolutize()
    {
        String base = "http://a/b/c/d;p?q";

        //
        // Test examples from RFC 2396
        //

        // normal cases
        assertEquals("g:h", SourceUtil.absolutize(base, "g:h"));
        assertEquals("http://a/b/c/g", SourceUtil.absolutize(base, "g"));
        assertEquals("http://a/b/c/g", SourceUtil.absolutize(base, "./g"));
        assertEquals("http://a/b/c/g/", SourceUtil.absolutize(base, "g/"));
        assertEquals("http://a/g", SourceUtil.absolutize(base, "/g"));
        assertEquals("http://g", SourceUtil.absolutize(base, "//g"));
        assertEquals("http://a/b/c/?y", SourceUtil.absolutize(base, "?y"));
        assertEquals("http://a/b/c/g?y", SourceUtil.absolutize(base, "g?y"));
        assertEquals("http://a/b/c/d;p?q#s", SourceUtil.absolutize(base, "#s"));
        assertEquals("http://a/b/c/g#s", SourceUtil.absolutize(base, "g#s"));
        assertEquals("http://a/b/c/g?y#s", SourceUtil.absolutize(base, "g?y#s"));
        assertEquals("http://a/b/c/;x", SourceUtil.absolutize(base, ";x"));
        assertEquals("http://a/b/c/g;x", SourceUtil.absolutize(base, "g;x"));
        assertEquals("http://a/b/c/g;x?y#s", SourceUtil.absolutize(base, "g;x?y#s"));
        assertEquals("http://a/b/c/", SourceUtil.absolutize(base, "."));
        assertEquals("http://a/b/c/", SourceUtil.absolutize(base, "./"));
        assertEquals("http://a/b/", SourceUtil.absolutize(base, ".."));
        assertEquals("http://a/b/", SourceUtil.absolutize(base, "../"));
        assertEquals("http://a/b/g", SourceUtil.absolutize(base, "../g"));
        assertEquals("http://a/", SourceUtil.absolutize(base, "../.."));
        assertEquals("http://a/", SourceUtil.absolutize(base, "../../"));
        assertEquals("http://a/g", SourceUtil.absolutize(base, "../../g"));

        // abnormal cases
        assertEquals("http://a/../g", SourceUtil.absolutize(base, "../../../g"));
        assertEquals("http://a/../../g", SourceUtil.absolutize(base, "../../../../g"));

        assertEquals("http://a/./g", SourceUtil.absolutize(base, "/./g"));
        assertEquals("http://a/../g", SourceUtil.absolutize(base, "/../g"));
        assertEquals("http://a/b/c/g.", SourceUtil.absolutize(base, "g."));
        assertEquals("http://a/b/c/.g", SourceUtil.absolutize(base, ".g"));
        assertEquals("http://a/b/c/g..", SourceUtil.absolutize(base, "g.."));
        assertEquals("http://a/b/c/..g", SourceUtil.absolutize(base, "..g"));

        assertEquals("http://a/b/g", SourceUtil.absolutize(base, "./../g"));
        assertEquals("http://a/b/c/g/", SourceUtil.absolutize(base, "./g/."));
        assertEquals("http://a/b/c/g/h", SourceUtil.absolutize(base, "g/./h"));
        assertEquals("http://a/b/c/h", SourceUtil.absolutize(base, "g/../h"));
        assertEquals("http://a/b/c/g;x=1/y", SourceUtil.absolutize(base, "g;x=1/./y"));
        assertEquals("http://a/b/c/y", SourceUtil.absolutize(base, "g;x=1/../y"));

        assertEquals("http://a/b/c/g?y/./x", SourceUtil.absolutize(base, "g?y/./x"));
        assertEquals("http://a/b/c/g?y/../x", SourceUtil.absolutize(base, "g?y/../x"));
        assertEquals("http://a/b/c/g#s/./x", SourceUtil.absolutize(base, "g#s/./x"));
        assertEquals("http://a/b/c/g#s/../x", SourceUtil.absolutize(base, "g#s/../x"));

        //
        // other tests
        //

        // if there's a scheme, url is absolute
        assertEquals("http://a", SourceUtil.absolutize("", "http://a"));
        assertEquals("cocoon:/a", SourceUtil.absolutize("", "cocoon:/a", true));

        // handle null base
        assertEquals("a", SourceUtil.absolutize(null, "a"));

        // handle network reference
        assertEquals("http://a/b", SourceUtil.absolutize("http://myhost", "//a/b"));

        // handle empty authority
        assertEquals("http:///a/b", SourceUtil.absolutize("http:///a/", "b"));

        // cocoon and context protocols
        assertEquals("cocoon://a/b/c", SourceUtil.absolutize("cocoon://a/b/", "c", true));
        assertEquals("cocoon:/a/b/c", SourceUtil.absolutize("cocoon:/a/b/", "c", true));
        assertEquals("cocoon://c", SourceUtil.absolutize("cocoon://a", "c", true));
        assertEquals("cocoon://c", SourceUtil.absolutize("cocoon://a/b/", "../../c", true));

        // Test relative File URI
        assertEquals("file://C:/projects/avalon-excalibur/build/docs/framework/api/index.html%3Ffoo=bar",
                SourceUtil.absolutize("file://C:/projects/avalon-excalibur/build/docs/framework/", "api/index.html%3Ffoo=bar"));
        assertEquals( "file://C:/foo/api/", SourceUtil.absolutize( "file://C:/foo/", "api/" ) );
    }
}
