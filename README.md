# vlingo-lattice

[![Build Status](https://travis-ci.org/vlingo/vlingo-lattice.svg?branch=master)](https://travis-ci.org/vlingo/vlingo-lattice) [ ![Download](https://api.bintray.com/packages/vlingo/vlingo-platform-java/vlingo-lattice/images/download.svg) ](https://bintray.com/vlingo/vlingo-platform-java/vlingo-lattice/_latestVersion)

Tooling for reactive Domain-Driven Design projects that are highly concurrent. Includes compute grid, actor caching, spaces, cross-node cluster messaging, CQRS, and Event Sourcing support.


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
      <artifactId>vlingo-lattice</artifactId>
      <version>0.7.1</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```

```gradle
dependencies {
    compile 'io.vlingo:vlingo-lattice:0.7.1'
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
