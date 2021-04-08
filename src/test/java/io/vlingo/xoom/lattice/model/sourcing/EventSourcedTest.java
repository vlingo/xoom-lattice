// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.lattice.model.sourcing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.actors.testkit.TestWorld;
import io.vlingo.xoom.lattice.model.DomainEvent;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.xoom.symbio.BaseEntry;
import io.vlingo.xoom.symbio.Entry;
import io.vlingo.xoom.symbio.EntryAdapterProvider;
import io.vlingo.xoom.symbio.store.journal.Journal;
import io.vlingo.xoom.symbio.store.journal.inmemory.InMemoryJournalActor;

public class EventSourcedTest {
  private Entity entity;
  private Journal<String> journal;
  private MockJournalDispatcher dispatcher;
  private SourcedTypeRegistry registry;
  private Result result;
  private TestWorld testWorld;
  private World world;

  @Test
  public void testThatCtorEmits() {
    final AccessSafely resultAccess = result.afterCompleting(2);
    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

    entity.doTest1();

    assertTrue(resultAccess.readFrom("tested1"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    BaseEntry<String> appendeAt0 = dispatcherAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.typeName());
    assertFalse(resultAccess.readFrom("tested2"));
  }

  @Test
  public void testThatCommandEmits() {
    final AccessSafely resultAccess = result.afterCompleting(2);
    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

    entity.doTest1();

    assertTrue(resultAccess.readFrom("tested1"));
    assertFalse(resultAccess.readFrom("tested2"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    BaseEntry<String> appendeAt0 = dispatcherAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.typeName());

    final AccessSafely resultAccess2 = result.afterCompleting(2);
    final AccessSafely dispatcherAccess2 = dispatcher.afterCompleting(1);

    entity.doTest2();

    assertEquals(2, (int) resultAccess2.readFrom("appliedCount"));
    assertEquals(2, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt1 = resultAccess2.readFrom("appliedAt", 1);
    assertNotNull(appliedAt1);
    assertEquals(Test2Happened.class, appliedAt1.getClass());
    BaseEntry<String> appendeAt1 = dispatcherAccess2.readFrom("appendedAt", 1);
    assertNotNull(appendeAt1);
    assertEquals(Test2Happened.class.getName(), appendeAt1.typeName());
  }

  @Test
  public void testThatOutcomeCompletes() {
    final AccessSafely resultAccess = result.afterCompleting(2);
    final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

    entity.doTest1();

    assertTrue(resultAccess.readFrom("tested1"));
    assertFalse(resultAccess.readFrom("tested3"));
    assertEquals(1, (int) resultAccess.readFrom("appliedCount"));
    assertEquals(1, (int) dispatcherAccess.readFrom("entriesCount"));
    Object appliedAt0 = resultAccess.readFrom("appliedAt", 0);
    assertNotNull(appliedAt0);
    assertEquals(Test1Happened.class, appliedAt0.getClass());
    BaseEntry<String> appendeAt0 = dispatcherAccess.readFrom("appendedAt", 0);
    assertNotNull(appendeAt0);
    assertEquals(Test1Happened.class.getName(), appendeAt0.typeName());

    final AccessSafely resultAccess2 = result.afterCompleting(2);
    final AccessSafely dispatcherAccess2 = dispatcher.afterCompleting(1);

    entity.doTest3().andThenConsume(greeting -> {
      assertEquals("hello", greeting);
    });

    assertEquals(2, (int) resultAccess2.readFrom("appliedCount"));
    assertEquals(2, (int) dispatcherAccess2.readFrom("entriesCount"));
    Object appliedAt1 = resultAccess2.readFrom("appliedAt", 1);
    assertNotNull(appliedAt1);
    assertEquals(Test3Happened.class, appliedAt1.getClass());
    BaseEntry<String> appendeAt1 = dispatcherAccess.readFrom("appendedAt", 1);
    assertNotNull(appendeAt1);
    assertEquals(Test3Happened.class.getName(), appendeAt1.typeName());
  }

  @Test
  public void testBaseClassBehavior() {
    final Product product = world.actorFor(Product.class, ProductEntity.class);

    final AccessSafely access = dispatcher.afterCompleting(4);

    product.define("dice", "fuz", "dice-fuz-1", "Fuzzy dice.", 999);

    product.declareType("Type1");

    product.categorize("Category2");

    product.changeName("Fuzzy, fuzzy dice!");

    final List<Entry<String>> entries = access.readFrom("entries");

    assertEquals("ProductDefined", innerToSimple(entries.get(0).typeName()));
    assertEquals("ProductTyped", innerToSimple(entries.get(1).typeName()));
    assertEquals("ProductCategorized", innerToSimple(entries.get(2).typeName()));
    assertEquals("ProductNameChanged", innerToSimple(entries.get(3).typeName()));
  }

  @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setUp() {
    testWorld = TestWorld.startWithDefaults("test-es");

    world = testWorld.world();

    dispatcher = new MockJournalDispatcher();

    EntryAdapterProvider entryAdapterProvider = EntryAdapterProvider.instance(world);

    entryAdapterProvider.registerAdapter(Test1Happened.class, new Test1HappenedAdapter());
    entryAdapterProvider.registerAdapter(Test2Happened.class, new Test2HappenedAdapter());
    entryAdapterProvider.registerAdapter(Test3Happened.class, new Test3HappenedAdapter());

    journal = world.actorFor(Journal.class, InMemoryJournalActor.class, Arrays.asList(dispatcher));

    registry = new SourcedTypeRegistry(world);
    registry.register(new Info(journal, TestEventSourcedEntity.class, TestEventSourcedEntity.class.getSimpleName()));
    registry.register(new Info(journal, ProductEntity.class, ProductEntity.class.getSimpleName()));
    registry.register(new Info(journal, ProductParent.class, ProductParent.class.getSimpleName()));
    registry.register(new Info(journal, ProductGrandparent.class, ProductGrandparent.class.getSimpleName()));

    result = new Result();
    entity = world.actorFor(Entity.class, TestEventSourcedEntity.class, result);
  }

  private String innerToSimple(final String fqcn) {
    final String simpleName = fqcn.substring(fqcn.lastIndexOf('$') + 1);
    return simpleName;
  }

  //===========================
  // HIERARCHICAL TEST TYPES
  //===========================

  public static interface Product {
    void define(final String type, final String category, final String name, final String description, final long price);
    void declareType(final String type);
    void categorize(final String category);
    void changeDescription(String description);
    void changeName(String name);
    void changePrice(long price);
  }

  public static abstract class ProductGrandparent extends EventSourced implements Product {
    private String type;

    public ProductGrandparent(String streamName) {
      super(streamName);
    }

    @Override
    public void declareType(final String type) {
      apply(new ProductTyped(type));
    }

    @Override
    public String toString() {
      return "Grandparent [type=" + type + "]";
    }

    private void whenProductTyped(final ProductTyped event) {
      this.type = event.type;
    }

    static {
      registerConsumer(ProductGrandparent.class, ProductTyped.class, ProductGrandparent::whenProductTyped);
    }
  }

  public static abstract class ProductParent extends ProductGrandparent {
    private String category;

    public ProductParent(String streamName) {
      super(streamName);
    }

    @Override
    public void categorize(final String category) {
      apply(new ProductCategorized(category));
    }

    @Override
    public String toString() {
      return "ProductParent [category=" + category + "]";
    }

    private void whenProductCategorized(final ProductCategorized event) {
      this.category = event.category;
    }

    static {
      registerConsumer(ProductParent.class, ProductCategorized.class, ProductParent::whenProductCategorized);
    }
  }

  public static class ProductEntity extends ProductParent {
    public String name;
    public String description;
    public long price;

    public ProductEntity() {
      super(null);
    }

    @Override
    public void define(String type, String category, String name, String description, long price) {
      apply(new ProductDefined(name, description, price));
    }

    /* (non-Javadoc)
     * @see io.vlingo.xoom.lattice.model.sourcing.Product#changeDescription(java.lang.String)
     */
    @Override
    public void changeDescription(final String description) {
      apply(new ProductDescriptionChanged(description));
    }

    /* (non-Javadoc)
     * @see io.vlingo.xoom.lattice.model.sourcing.Product#changeName(java.lang.String)
     */
    @Override
    public void changeName(final String name) {
      apply(new ProductNameChanged(name));
    }

    /* (non-Javadoc)
     * @see io.vlingo.xoom.lattice.model.sourcing.Product#changePrice(long)
     */
    @Override
    public void changePrice(final long price) {
      apply(new ProductPriceChanged(price));
    }

    public void whenProductDefined(final ProductDefined event) {
      this.name = event.name;
      this.description = event.description;
      this.price = event.price;
    }

    public void whenProductDescriptionChanged(final ProductDescriptionChanged event) {
      this.description = event.description;
    }

    public void whenProductNameChanged(final ProductNameChanged event) {
      this.name = event.name;
    }

    public void whenProductPriceChanged(final ProductPriceChanged event) {
      this.price = event.price;
    }

    static {
      registerConsumer(ProductEntity.class, ProductDefined.class, ProductEntity::whenProductDefined);
      registerConsumer(ProductEntity.class, ProductDescriptionChanged.class, ProductEntity::whenProductDescriptionChanged);
      registerConsumer(ProductEntity.class, ProductNameChanged.class, ProductEntity::whenProductNameChanged);
      registerConsumer(ProductEntity.class, ProductPriceChanged.class, ProductEntity::whenProductPriceChanged);
    }
  }

  public static final class ProductTyped extends DomainEvent {
    public final String type;

    public ProductTyped(final String type) {
      this.type = type;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductTyped.class) {
        return false;
      }

      final ProductTyped otherProductTyped = (ProductTyped) other;

      return this.type.equals(otherProductTyped.type);
    }
  }

  public static final class ProductCategorized extends DomainEvent {
    public final String category;

    public ProductCategorized(final String category) {
      this.category = category;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductCategorized.class) {
        return false;
      }

      final ProductCategorized otherProductCategorized = (ProductCategorized) other;

      return this.category.equals(otherProductCategorized.category);
    }
  }

  public static final class ProductDefined extends DomainEvent {
    public final String description;
    public final String name;
    public final Date occurredOn;
    public final long price;
    public final int version;

    ProductDefined(final String name, final String description, final long price) {
      this.name = name;
      this.description = description;
      this.price = price;
      this.occurredOn = new Date();
      this.version = 1;
    }

    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductDefined.class) {
        return false;
      }

      final ProductDefined otherProductDefined = (ProductDefined) other;

      return this.name.equals(otherProductDefined.name) &&
          this.description.equals(otherProductDefined.description) &&
          this.price == otherProductDefined.price &&
          this.version == otherProductDefined.version;
    }
  }

  public static final class ProductDescriptionChanged extends DomainEvent {
    public final String description;
    public final Date occurredOn;
    public final int version;

    ProductDescriptionChanged(final String description) {
      this.description = description;
      this.occurredOn = new Date();
      this.version = 1;
    }

    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductDescriptionChanged.class) {
        return false;
      }

      final ProductDescriptionChanged otherProductDescriptionChanged = (ProductDescriptionChanged) other;

      return this.description.equals(otherProductDescriptionChanged.description) &&
          this.version == otherProductDescriptionChanged.version;
    }
  }

  public static final class ProductNameChanged extends DomainEvent {
    public final String name;
    public final Date occurredOn;
    public final int version;

    ProductNameChanged(final String name) {
      this.name = name;
      this.occurredOn = new Date();
      this.version = 1;
    }

    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductNameChanged.class) {
        return false;
      }

      final ProductNameChanged otherProductNameChanged = (ProductNameChanged) other;

      return this.name.equals(otherProductNameChanged.name) &&
          this.version == otherProductNameChanged.version;
    }
  }

  public static final class ProductPriceChanged extends DomainEvent {
    public final long price;
    public final Date occurredOn;
    public final int version;

    ProductPriceChanged(final long price) {
      this.price = price;
      this.occurredOn = new Date();
      this.version = 1;
    }

    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductPriceChanged.class) {
        return false;
      }

      final ProductPriceChanged otherProductPriceChanged = (ProductPriceChanged) other;

      return this.price == otherProductPriceChanged.price &&
          this.version == otherProductPriceChanged.version;
    }
  }
}
