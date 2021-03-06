package org.infinispan.conflict.impl;

import org.infinispan.conflict.MergePolicy;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "partitionhandling.MergePolicyPreferredAlwaysTest")
public class MergePolicyPreferredAlwaysTest extends BaseMergePolicyTest {

   @Override
   public Object[] factory() {
      return new Object[] {
            new MergePolicyPreferredAlwaysTest().setPartitions("5N", new int[]{0,1,2}, new int[]{3,4}),
            new MergePolicyPreferredAlwaysTest().setPartitions("4N", new int[]{0,1}, new int[]{2,3}),
            new MergePolicyPreferredAlwaysTest().setPartitions("3N", new int[]{0,1}, new int[]{2}).setValueAfterMerge("BEFORE SPLIT")
      };
   }

   public MergePolicyPreferredAlwaysTest() {
      super();
      this.mergePolicy = MergePolicy.PREFERRED_ALWAYS;
   }
}
