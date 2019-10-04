# vlingo-lattice-core

[![Javadocs](http://javadoc.io/badge/io.vlingo/vlingo-lattice-core.svg?color=brightgreen)](http://javadoc.io/doc/io.vlingo/vlingo-lattice-core) [![Build Status](https://travis-ci.org/vlingo/vlingo-lattice-core.svg?branch=master)](https://travis-ci.org/vlingo/vlingo-lattice-core) [ ![Download](https://api.bintray.com/packages/vlingo/vlingo-platform-java/vlingo-lattice-core/images/download.svg) ](https://bintray.com/vlingo/vlingo-platform-java/vlingo-lattice-core/_latestVersion) [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/vlingo-platform-java/lattice)

The vlingo/PLATFORM tooling for reactive Domain-Driven Design models that are highly concurrent. Includes compute grid, actor caching, spaces, cross-node cluster messaging, message exchanges, CQRS, and Event Sourcing support.

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
      <artifactId>vlingo-lattice-core</artifactId>
      <version>0.8.9</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```

```gradle
dependencies {
    compile 'io.vlingo:vlingo-lattice-core:0.8.9'
}

repositories {
    jcenter()
}
```

License (See LICENSE file for full license)
-------------------------------------------
Copyright © 2012-2018 Vaughn Vernon. All rights reserved.

This Source Code Form is subject to the terms of the
Mozilla Public License, v. 2.0. If a copy of the MPL
was not distributed with this file, You can obtain
one at https://mozilla.org/MPL/2.0/.


### Licenses for Dependencies

MurmurHash.java is open source licensed under Apache 2 by the Apache Software Foundation
http://www.apache.org/licenses/LICENSE-2.0

See: https://github.com/apache/cassandra/blob/trunk/src/java/org/apache/cassandra/utils/MurmurHash.java
