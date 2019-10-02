# vlingo-lattice-exchange-rabbitmq

[![Javadocs](http://javadoc.io/badge/io.vlingo/vlingo-lattice-exchange-rabbitmq.svg?color=brightgreen)](http://javadoc.io/doc/io.vlingo/vlingo-lattice-exchange-rabbitmq) [![Build Status](https://travis-ci.org/vlingo/vlingo-lattice-exchange-rabbitmq.svg?branch=master)](https://travis-ci.org/vlingo/vlingo-lattice-exchange-rabbitmq) [ ![Download](https://api.bintray.com/packages/vlingo/vlingo-platform-java/vlingo-lattice-exchange-rabbitmq/images/download.svg) ](https://bintray.com/vlingo/vlingo-platform-java/vlingo-lattice-exchange-rabbitmq/_latestVersion) [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/vlingo-platform-java/lattice)


The vlingo/PLATFORM implementation of vlingo/lattice Exchange for RabbitMQ.

See the primary protocol and related ones:
- [io.vlingo.lattice.exchange.Exchange](https://github.com/vlingo/vlingo-lattice/blob/master/src/main/java/io/vlingo/lattice/exchange/Exchange.java)

See the following tests for examples:
- [io.vlingo.lattice.exchange.rabbitmq.ExchangeFactoryTest](https://github.com/vlingo/vlingo-lattice-exchange-rabbitmq/blob/master/src/test/java/io/vlingo/lattice/exchange/rabbitmq/ExchangeFactoryTest.java)
- [io.vlingo.lattice.exchange.rabbitmq.FanOutExchangeTest](https://github.com/vlingo/vlingo-lattice-exchange-rabbitmq/blob/master/src/test/java/io/vlingo/lattice/exchange/rabbitmq/FanOutExchangeTest.java)

### Bintray

```xml
  <repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
      <groupId>io.vlingo</groupId>
      <artifactId>vlingo-lattice-exchange-rabbitmq</artifactId>
      <version>0.8.9</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```

```gradle
dependencies {
    compile 'io.vlingo:vlingo-lattice-exchange-rabbitmq:0.8.9'
}

repositories {
    jcenter()
}
```

## Docker and Bouncing the Server Volume
RabbitMQ must be running for tests. See the `rmqbounce.sh`. This shell script can be used to bounce the RabbitMQ volume named in `docker-compose.yml`:

  `vlingo-lattice-exchange-rabbitmq`

`$ ./rmqbounce.sh`


License (See LICENSE file for full license)
-------------------------------------------
Copyright © 2012-2018 Vaughn Vernon. All rights reserved.

This Source Code Form is subject to the terms of the
Mozilla Public License, v. 2.0. If a copy of the MPL
was not distributed with this file, You can obtain
one at https://mozilla.org/MPL/2.0/.
