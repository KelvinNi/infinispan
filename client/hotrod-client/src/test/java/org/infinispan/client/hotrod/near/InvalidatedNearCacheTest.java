package org.infinispan.client.hotrod.near;

import static org.infinispan.client.hotrod.test.HotRodClientTestingUtil.withRemoteCacheManager;
import static org.testng.AssertJUnit.assertEquals;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.test.RemoteCacheManagerCallable;
import org.infinispan.client.hotrod.test.SingleHotRodServerTest;
import org.infinispan.commons.CacheConfigurationException;
import org.testng.annotations.Test;

@Test(groups = {"functional", "smoke"}, testName = "client.hotrod.near.InvalidatedNearCacheTest")
public class InvalidatedNearCacheTest extends SingleHotRodServerTest {

   AssertsNearCache<Integer, String> assertClient;

   @Override
   protected RemoteCacheManager getRemoteCacheManager() {
      assertClient = createAssertClient();
      return assertClient.manager;
   }

   protected <K, V> AssertsNearCache<K, V> createAssertClient() {
      ConfigurationBuilder builder = clientConfiguration();
      return AssertsNearCache.create(this.<byte[], Object>cache(), builder);
   }

   protected <K, V> RemoteCache<K, V> createClient() {
      ConfigurationBuilder builder = clientConfiguration();
      return new RemoteCacheManager(builder.build()).getCache();
   }

   private ConfigurationBuilder clientConfiguration() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(hotrodServer.getPort());
      builder.nearCache().mode(getNearCacheMode()).maxEntries(-1);
      return builder;
   }

   protected NearCacheMode getNearCacheMode() {
      return NearCacheMode.INVALIDATED;
   }

   public void testGetNearCacheAfterConnect() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(hotrodServer.getPort());
      RemoteCacheManager manager = new RemoteCacheManager(builder.build());
      RemoteCache cache = manager.getCache();
      cache.put(1, "one");
      cache.put(2, "two");

      builder.nearCache().mode(getNearCacheMode()).maxEntries(-1);
      assertClient = AssertsNearCache.create(this.<byte[], Object>cache(), builder);

      assertEquals(2, assertClient.remote.size());
      assertClient.expectNoNearEvents();
      assertClient.get(1, "one").expectNearGetValue(1, null).expectNearPutIfAbsent(1, "one");
      assertClient.get(2, "two").expectNearGetValue(2, null).expectNearPutIfAbsent(2, "two");
      assertClient.remove(1).expectNearRemove(1);
      assertClient.remove(2).expectNearRemove(2);
   }

   public void testGetNearCache() {
      assertClient.expectNoNearEvents();
      assertClient.get(1, null).expectNearGetNull(1);
      assertClient.put(1, "v1").expectNearRemove(1);
      assertClient.get(1, "v1").expectNearGetNull(1).expectNearPutIfAbsent(1, "v1");
      assertClient.get(1, "v1").expectNearGetValue(1, "v1");
      assertClient.remove(1).expectNearRemove(1);
      assertClient.get(1, null).expectNearGetNull(1);
   }

   public void testGetVersionedNearCache() {
      assertClient.expectNoNearEvents();
      assertClient.getVersioned(1, null).expectNearGetNull(1);
      assertClient.put(1, "v1").expectNearRemove(1);
      assertClient.getVersioned(1, "v1").expectNearGetNull(1).expectNearPutIfAbsent(1, "v1");
      assertClient.getVersioned(1, "v1").expectNearGetValueVersion(1, "v1");
      assertClient.remove(1).expectNearRemove(1);
      assertClient.getVersioned(1, null).expectNearGetNull(1);
   }

   public void testUpdateNearCache() {
      assertClient.expectNoNearEvents();
      assertClient.put(1, "v1").expectNearRemove(1);
      assertClient.put(1, "v2").expectNearRemove(1);
      assertClient.get(1, "v2").expectNearGetNull(1).expectNearPutIfAbsent(1, "v2");
      assertClient.get(1, "v2").expectNearGetValue(1, "v2");
      assertClient.put(1, "v3").expectNearRemove(1);
      assertClient.remove(1).expectNearRemove(1);
   }

   public void testGetUpdatesNearCache() {
      assertClient.expectNoNearEvents();
      assertClient.put(1, "v1").expectNearRemove(1);

      final AssertsNearCache<Integer, String> newAsserts = createAssertClient();
      withRemoteCacheManager(new RemoteCacheManagerCallable(newAsserts.manager) {
         @Override
         public void call() {
            newAsserts.expectNoNearEvents();
            newAsserts.get(1, "v1").expectNearGetNull(1).expectNearPutIfAbsent(1, "v1");
         }
      });
   }

   @Test(expectedExceptions = CacheConfigurationException.class,
         expectedExceptionsMessageRegExp = ".*When enabling near caching, number of max entries must be configured.*")
   public void testConfigurationWithoutMaxEntries() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addServer().host("127.0.0.1").port(hotrodServer.getPort());
      builder.nearCache().mode(getNearCacheMode());
      new RemoteCacheManager(builder.build());
   }

}
