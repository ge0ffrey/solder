/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.seam.solder.test.log;

import static org.jboss.seam.solder.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertEquals;

import javax.enterprise.inject.Instance;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies the integration with JBoss Logging, for both raw and type-safe injections.
 * 
 * <p>
 * NOTE: Some of these tests must be verified manually as we have no way to plug
 * in a mock logger.
 * </p>
 * 
 * @author David Allen
 * @author Dan Allen
 */
@RunWith(Arquillian.class)
public class LoggerInjectionTest
{
   @Deployment
   public static Archive<?> deployment()
   {
      return baseDeployment().addPackage(LoggerInjectionTest.class.getPackage());
   }

   @Test
   public void testLoggerInjection(Sparrow sparrow)
   {
      sparrow.generateLogMessage();
      Assert.assertEquals(Sparrow.class.getName(), sparrow.getLogger().getName());
   }

   @Test
   public void testLoggerInjectionWithCategory(Finch finch)
   {
      finch.generateLogMessage();
      Assert.assertEquals("Finch", finch.getLogger().getName());
   }
   
   @Test
   public void testLoggerInjectionWithTypedCategory(Wren wren)
   {
      wren.generateLogMessage();
      Assert.assertEquals(BirdLogger.class.getName(), wren.getLogger().getName());
   }
   
   @Test
   public void testLoggerInjectionWithSuffix(Raven raven)
   {
      raven.generateLogMessage();
      Assert.assertEquals(Raven.class.getName() + ".log", raven.getLogger().getName());
   }
   
   @Test(expected = IllegalStateException.class)
   public void testMessageLoggerInjection(Instance<Owl> owlResolver)
   {
      owlResolver.get().generateLogMessage();
   }
   
   @Test
   public void testMessageLoggerInjectionWithCategory(Hawk hawk)
   {
      hawk.generateLogMessage();
   }
   
   @Test
   public void testMessageBundleInjection(Jay jay)
   {
      assertEquals("Spotted 8 jays", jay.getMessage());
   }
   
}
