# Datarouter

Datarouter is a Java serialization layer that gives developers a simple interface to a variety of databases in complex configurations.  It sits on top of other database access libraries and encourages access through a standard set of methods allowing an application to mix and match databases while staying independent of any particular database technology.  The application is programmed against Datarouter’s interfaces and can leave JDBC, Memcached, HBase, etc as runtime dependencies.  Or, developers can choose to code directly against a specific database in performance critical situations, still leaving the vast majority of the code in a portable state.  While providing this level of abstraction, it also adds a rich set of monitoring and scalability features.

## Components

Datarouter is composed of several constructs:
- Databean: the primary unit of storage
- PrimaryKey: an object that uniquely identifies a databean
- IndexEntry: a databean representing a secondary index entry
- Node: an interface through which you put and get databeans
- Client: a physical connection to a datastore
- Datarouter: a container for clients and nodes
- Router: a collection of related nodes

## Design goals:
- internally rely more on normal compile-safe java constructs like interfaces and composition rather than “convenient” reflection and annotations.  this makes datarouter code easier for users to understand and debug
- encourage strong typing that helps refactoring and keeping a nimble code base, both in user applications and datarouter itself
- subclasses of Databean are the primary data definition language
  - take advantage of refactoring tools like Eclipse when the Databeans change
  - a schema-update utility helps to create tables and add and drop columns and indexes
  - keep all field and index definitions together in the Databean file to minimize mistakes when adding, removing, or modifying fields
- encourage database access through simple methods that are common to many datastores
  - put, get, delete, getMulti, getWithPrefix, iterateKeys, iterate, etc
  - discourage joins or other complex queries that may not scale predictably
  - but allow any type of query to the underlying datastore if it’s deemed necessary
- allow Databeans to be easily moved between or straddle datastores like MySQL, HBase and Memcached
  - to keep an application modern as database technologies advance
  - to avoid lock-in
  - to transparently span multiple technologies, like easily putting a memcached cache in front of certain mysql tables
- hide tiered storage hierarchies behind a simple “Node” interface
  - a PhysicalNode represents a storage location
  - PhysicalNodes can be combined to form complex Node hierarchies: 
    - caching, replication, audit logging, buffering, redundant storage, horizontal or vertical partitioning, redundant read requests, etc
  - these hierarchies are built using compiler-checked Java, but are more like configuration than code
changes to Node hierarchies or the PhysicalNodes backing them are transparent to the application code using the Nodes
- natively support secondary indexes common to relational and other databases
  - define indexes with strongly typed IndexEntry databeans
  - enforce proper index usage by developers and make it explicit where code relies on an index for performance
  - allow iteration of Databeans in order of some indexed columns

