package org.infinispan.distribution;

import org.infinispan.config.Configuration;
import org.infinispan.remoting.transport.Address;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Mircea.Markus@jboss.com
 * @since 4.2
 */
@Test (groups = "functional", testName = "distribution.DistributionManagerImplTest")
public class DistributionManagerImplTest extends AbstractInfinispanTest {
   List<Address> servers;
   DefaultConsistentHash ch;
   Address a0;
   Address a1;
   Address a2;
   Address a3;
   Address a4;

   @BeforeTest
   public void setUp() {
      servers = new LinkedList<Address>();
      int numServers = 5;
      for (int i = 0; i < numServers; i++) {
         servers.add(new TestAddress(i));
      }
      ch = (DefaultConsistentHash) BaseDistFunctionalTest.createNewConsistentHash(servers);
      a0 = ch.getAddressOnTheWheel().get(0);
      a1 = ch.getAddressOnTheWheel().get(1);
      a2 = ch.getAddressOnTheWheel().get(2);
      a3 = ch.getAddressOnTheWheel().get(3);
      a4 = ch.getAddressOnTheWheel().get(4);
   }


   /**
    * numOwners = 3. Let's a a2 leaves.
    */
   public void testLeaver() {
      DistributionManagerImpl dm = newDM(3);
      dm.setSelf(a0);
      assert dm.willReceiveLeaverState(a2) : " a0 will be the 2nd backup for a3's state";
      dm.setSelf(a1);
      assert !dm.willReceiveLeaverState(a2) : " a1 is not affected";
      dm.setSelf(a3);
      assert dm.willReceiveLeaverState(a2) : "this needs to receive state from a0";
      dm.setSelf(a4);
      assert dm.willReceiveLeaverState(a2) : "this needs to receive state from a1";
   }

   public void testWillSendLeaverState() {
      DistributionManagerImpl dm = newDM(3);
      dm.setSelf(a0);
      assert dm.willSendLeaverState(a2) : " a0 will need to send state to a3 so that it has 3 backups";
      dm.setSelf(a1);
      assert dm.willSendLeaverState(a2) : " a1 backups to a4";
      dm.setSelf(a3);
      assert dm.willSendLeaverState(a2) : "needs to send a2 state to a0 (new state backup)";
      dm.setSelf(a4);
      assert !dm.willSendLeaverState(a2) : "don't need to send any state as its backups are not affected";
   }

   private DistributionManagerImpl newDM(int numOwners) {
      DistributionManagerImpl dm = new DistributionManagerImpl();
      Configuration configuration = new Configuration();
      configuration.setNumOwners(numOwners);
      dm.setConfiguration(configuration);
      dm.setConsistentHash(ch);
      return dm;
   }
}
