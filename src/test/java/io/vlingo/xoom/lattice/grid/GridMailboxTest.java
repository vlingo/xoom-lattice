package io.vlingo.xoom.lattice.grid;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.junit.Test;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.plugin.mailbox.testkit.TestMailbox;
import io.vlingo.xoom.lattice.grid.cache.Cache;
import io.vlingo.xoom.lattice.grid.cache.CacheNodePoint;
import io.vlingo.xoom.lattice.grid.hashring.HashRing;
import io.vlingo.xoom.lattice.grid.hashring.HashedNodePoint;
import io.vlingo.xoom.lattice.grid.hashring.MD5ArrayHashRing;
import io.vlingo.xoom.lattice.grid.hashring.MurmurArrayHashRing;
import io.vlingo.xoom.wire.node.Id;

public class GridMailboxTest {

  final Cache cache = Cache.defaultCache();
  final BiFunction<Integer, Id, HashedNodePoint<Id>> factory =  (hash, node) -> new CacheNodePoint<Id>(cache, hash, node);

  @Test
  @SuppressWarnings("unused")
  public void testMurmurArrayHashRing() {
    HashRing<Id> hashRing = new MurmurArrayHashRing<>(100, factory);

    Id localId = Id.of(1);
    Mailbox localMailbox = new TestMailbox();
    Address address = new GridAddress(UUID.randomUUID());

    hashRing.includeNode(localId);
    hashRing.includeNode(Id.of(2));
    hashRing.includeNode(Id.of(3));
    hashRing.includeNode(Id.of(4));

    Consumer<Returns<?>> returnsConsumer = (r) -> {};

    GridMailbox gridMailbox = new GridMailbox(localMailbox, localId, address, hashRing, null);

    gridMailbox.isClosed();
    gridMailbox.close();
    gridMailbox.concurrencyCapacity();
  }

  @Test
  @SuppressWarnings("unused")
  public void testMD5ArrayHashRing() throws Exception {
    HashRing<Id> hashRing = new MD5ArrayHashRing<>(100, factory);

    Id localId = Id.of(1);
    Mailbox localMailbox = new TestMailbox();
    Address address = new GridAddress(UUID.randomUUID());

    hashRing.includeNode(localId);
    hashRing.includeNode(Id.of(2));

    Consumer<Returns<?>> returnsConsumer = (r) -> {};

    GridMailbox gridMailbox = new GridMailbox(localMailbox, localId, address, hashRing, null);

    System.out.println(gridMailbox.isClosed());
    System.out.println(gridMailbox.concurrencyCapacity());
    System.out.println(gridMailbox.isPreallocated());
  }

}
