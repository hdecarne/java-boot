/*
 * Copyright (c) 2018 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.boot.test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.boot.ApplicationJarClassLoader;

/**
 * Test {@linkplain ApplicationJarClassLoader} class.
 */
class ApplicationJarClassLoaderTest {

	@Test
	void testClassLoader() throws IOException, ReflectiveOperationException {
		ClassLoader bootstrapClassloader = getClass().getClassLoader();
		URL testJarUrl = bootstrapClassloader.getResource("test.jar");

		try (URLClassLoader testResourceLoader = new URLClassLoader(new URL[] { testJarUrl }, bootstrapClassloader);
				ApplicationJarClassLoader applicationClassloader = new ApplicationJarClassLoader(testJarUrl,
						testResourceLoader, ApplicationJarClassLoader.filter(false))) {
			for (URL classpathUrl : applicationClassloader.getURLs()) {
				System.out.println(classpathUrl.toExternalForm());
			}

			// Load a class with dependencies to an included Jar
			String testJarClass1Name = "de.carne.certmgr.certs.x509.X509CertificateHelper";
			Class<?> testJarClass1 = Class.forName(testJarClass1Name, false, applicationClassloader);

			Assertions.assertEquals(applicationClassloader, testJarClass1.getClassLoader());
			Assertions.assertEquals(testJarClass1Name, testJarClass1.getName());

			// Load a class from an included Jar
			String testJarClass2Name = "org.bouncycastle.LICENSE";
			Class<?> testJarClass2 = Class.forName(testJarClass2Name, false, applicationClassloader);

			Assertions.assertEquals(applicationClassloader, testJarClass2.getClassLoader());
			Assertions.assertEquals(testJarClass2Name, testJarClass2.getName());
		}
	}

}
