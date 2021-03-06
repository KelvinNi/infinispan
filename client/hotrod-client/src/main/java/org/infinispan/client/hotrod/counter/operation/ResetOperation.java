package org.infinispan.client.hotrod.counter.operation;

import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.impl.protocol.Codec;
import org.infinispan.client.hotrod.impl.protocol.HeaderParams;
import org.infinispan.client.hotrod.impl.transport.Transport;
import org.infinispan.client.hotrod.impl.transport.TransportFactory;
import org.infinispan.counter.api.StrongCounter;
import org.infinispan.counter.api.WeakCounter;

/**
 * A counter operation for {@link StrongCounter#reset()} and {@link WeakCounter#reset()}.
 *
 * @author Pedro Ruivo
 * @since 9.2
 */
public class ResetOperation extends BaseCounterOperation<Void> {

   public ResetOperation(Codec codec, TransportFactory transportFactory, AtomicInteger topologyId, Configuration cfg,
         String counterName) {
      super(codec, transportFactory, topologyId, cfg, counterName);
   }

   @Override
   protected Void executeOperation(Transport transport) {
      HeaderParams header = writeHeaderAndCounterName(transport, COUNTER_RESET_REQUEST);
      transport.flush();
      readHeaderAndValidateCounter(transport, header);
      return null;
   }
}
