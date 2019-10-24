# Datarouter

## Overview

Datarouter is a Java database abstraction layer that gives developers a simple interface to a variety of databases. It
sits on top of other database client libraries and encourages access through a standard set of methods allowing an
application to mix and match databases while staying independent of any particular database product. The application
is programmed against Datarouter’s interfaces while JDBC, Memcached, HBase, etc are left as swappable dependencies.
Developers can also choose to code directly against a specific database in performance critical situations or when 
using unique features of a particular product, still leaving the vast majority of the code in a portable state.

The first goal of Datarouter is to encourage type-safety and early error detection in all layers of your application so 
that it remains easy to refactor as it grows. Waiting for the results of an integration or even unit test suite is too 
slow for the frequent refactoring that's needed to quickly evolve an application beyond the original design goals. We 
want to catch errors at compile time, particularly when the Eclipse incremental compiler runs, and give your IDE the 
ability to automatically change large amounts of code.

The second goal is to encourage safe interactions with your database, making it easy to fetch data in a way that won't 
trigger expensive queries and making it harder to accidentally cause large tablescans or joins that can affect the overall
application health.

The third goal is the portability mentioned above. Knowing your application is portable from the get-go means you can 
jump in and start writing code without spending a lot of time debating which database you'll use or which cloud provider
you'll be tied to. A program can start out with a traditional relational database like MySQL, and as certain tables 
grow they can be offloaded to more scalable systems after trivial code changes. Or you might run on multiple systems 
at the same time, such as a multi-cloud application that uses Aurora, SQS, and Elasticache Memcached in Amazon's cloud 
while also using Cloud SQL, Cloud Pub/Sub and Memcached in Google's cloud.

Datarouter includes a web framework (datarouter-web) that aims to be lightweight and to rely on Java (not XML, JSON, 
or plain text) for configuring things like web request handler mappings. Additional (not yet released) modules like
datarouter-exception, datarouter-job, datarouter-trace, etc 
include many building blocks of a web app like log management, authentication, cron triggering, exception recording, counters, etc. 
Combining the storage and web frameworks and the added utils allows for building portable web apps that are easy to 
move between different database engines. For example, you could record exception stack traces to MySQL on your laptop, 
to HBase in your datacenter, DynamoDB when running on AWS, or Bigtable when running on Google Cloud.

## Components

The storage framework is composed of several constructs:
- Databean: the primary unit of storage
- PrimaryKey: an object that uniquely identifies a databean
- IndexEntry: a databean representing a secondary index entry
- Node: an interface through which you put, get, scan, and delete databeans
- Client: a physical connection to a datastore
- Datarouter: a container for clients and nodes
- Router: a collection of related nodes

## Design goals

- internally rely more on normal compile-safe Java constructs like interfaces and composition rather than “convenient” 
reflection and annotations. This makes datarouter code easier for users to understand and debug
- encourage strong typing that helps refactoring and keeping a nimble code base, both in user applications and 
datarouter itself
- subclasses of Databean are the primary data definition language
  - take advantage of refactoring tools like Eclipse when the Databeans change
  - a schema-update utility helps to create tables and add and drop columns and indexes
  - keep all field and index definitions together in the Databean file to minimize mistakes when adding, removing, or 
  modifying fields
- encourage database access through simple methods that are common to many datastores
  - put, get, delete, getMulti, getWithPrefix, scanKeys, scan, etc
  - discourage joins or other complex queries that may not scale predictably
  - but allow any type of query to the underlying datastore if it’s deemed necessary
- allow Databeans to be easily moved between or straddle datastores like MySQL, HBase and Memcached
  - to keep an application modern as database technologies advance
  - to avoid lock-in
  - to transparently span multiple technologies, like easily putting a memcached cache in front of certain mysql tables
- hide tiered storage hierarchies behind a simple “Node” interface
  - a PhysicalNode represents a storage location
  - PhysicalNodes can be combined to form complex Node hierarchies: 
    - caching, replication, audit logging, buffering, redundant storage, horizontal or vertical partitioning, redundant 
    read requests, etc
  - these hierarchies are built using compiler-checked Java, but are more like configuration than code changes to Node 
  hierarchies or the PhysicalNodes backing them are transparent to the application code using the Nodes
- natively support secondary indexes common to relational and other databases
  - define indexes with strongly typed IndexEntry databeans
  - enforce proper index usage by developers and make it explicit where code relies on an index for performance
  - allow iteration of Databeans in order of some indexed columns

## Design choices

Datarouter is an opinionated framework. This section explains the reasons behind some of the design choices, including
why datarouter-storage interfaces seem very restrictive compared to the SQL language. To help explain, it describes the
internals of MySQL in a simplified way and compares with other Java ORMs.

### How does MySQL store data with InnoDB?
A schema definition is held in memory for each table. Each row containing numbers, strings, dates, etc is serialized 
into a byte array. The rows are sorted by the primary key fields. Sorting is interpreted through the schema, applying 
data types, charsets, collations, etc. 

Rows are grouped into *pages* with a max size of 16 KB, roughly the size of a page in a phonebook. Any modification 
causes the whole page to be rewritten and stored in the InnoDB page cache (the `buffer pool`). The page cache can be 
many GB, and dirty pages are flushed to disk in the background. A row insertion causing the page to exceed 16 KB will result 
in the page being split in half causing ~8 KB of empty space at the end of each page.  Picture a phone book where the bottom
1/4 of each page is blank. This fragmentation is rarely corrected, done by a manual and expensive `OPTIMIZE TABLE`. 

Queries served by the page cache can be answered relatively quickly, while queries needing many disk reads are much slower. 
A query reading one row will load the whole page into memory. Pages are located using a B+ Tree index. The B+ Tree index
is composed of more pages.

If 100 primary keys fit in a page, then a 5,000,000 row table will have:
- 50,000 leaf pages
- 500 level 1 pages
- 5 level 2 pages
- 1 level 3 page containing 5 PKs

The higher level PK index pages tend to be accessed frequently and remain in the InnoDB page cache.

### What is a secondary index?
A secondary index is a separate physical table governed by the same schema as the primary table but with a subset of the 
columns and a different sort order. Each secondary index row must contain all of the columns of the PK. InnoDB will 
silently insert any unspecified columns. A “covering” index query selects data only from the index fields. A 
non-covering query will use the PK fields to do a B+ Tree lookup in the parent table.

### Why does sorting matter?
Older disks (HDDs) can read sequentially at 160 MBps, or 10,000 InnoDB pages/sec, while random access is limited to 
~100 pages/sec. SSDs are much better at random access, but the OS is still optimized for sequential reads resulting in 
10x better performance. The database can pre-fetch pages that will be needed by a large query.
The OS can combine many small fetches into fewer large fetches and do further pre-fetching. SSDs will also last longer 
if you write to them sequentially.

Example query costs with 100 rows per page:
- Fetching 10,000 sequential rows will fetch 100 pages
- Fetching 10,000 random rows may fetch 10,000 pages
- Fetching 10,000 covering index rows will fetch 100 index pages
- Fetching 10,000 non-covering index rows will fetch 100 index pages plus as many as 10,000 parent pages

### Datarouter is greedy about network usage
It always brings the full PK or Databean over the network, even when you don’t need all of the columns. Use only the PK 
when possible. This is to simplify the application by dealing only in strongly-typed databeans rather than specifying 
individual columns all over the code.
InnoDB is a row-oriented datastore, keeping all columns of a row serialized together. If you select only one column, 
you still incur the expense of fetching all other columns from the database’s disk and storing those columns in the page 
cache. Since the work of fetching the unneeded columns from disk and storing them in memory is already done, we might as 
well send them back to the client.

*What about very large columns?*
InnoDB has an optimization to store these in special pages that can be lazily fetched. We could write custom SQL that 
omits the large columns, but this breaks our databean abstraction. A better option can be to move the large columns to a 
separate databean/table that is accessed explicitly when needed.

Network usage is not generally an issue, but if it is, it’s likely due to one or just a few out of hundreds or thousands 
of queries. The problematic query could be optimized with custom SQL leaving the many other queries with the benefits 
of the databean abstraction.

### Datarouter does not skip rows
Let’s use a phone book as an example:
 - Primary key fields: lastName, firstName
 - Other fields: phoneNumber
 - Bytes per record: ~25
 - Records per page: ~400
 - Bytes per page: ~10,000

 Supported methods:
   - `MapStorage`
     - `exists`: return true if there is a record for the person
     - `getMultiKeys`: return the PKs that exist
     - `get`: get a single person by PK (lastName, firstName) including the phone number
     - `getMulti`: get multiple records by PK (this may read from many database pages)
   - `SortedStorage`
     - `scanKeys`: return all firstName/lastNames from startKey to stopKey
     - `scan`: return all records from startKey to stopKey
     - `scanWithPrefix`: return records based on one of these options:
       - lastName with wildcard suffix
       - full lastName
       - full lastName plus firstName with wildcard suffix
       - full lastName plus full firstName

Note that there is not a built-in method to return records by specifying the firstName. That’s because the query is not 
efficiently supported by the database - it would need to skip rows looking for a firstName match. While a query to 
collect the first 10 people named “Matthew” may execute quickly, a similar query for “Calixte” may scan millions of rows 
without finding 10 matches. Because of that, datarouter doesn’t support queries without knowing the leftmost portion of 
the PK.

Imagine executing the firstName query manually on a phone book - you’d have to look at every row on every page. It’s not fun for 
a person nor a database. Relational databases can magically answer any complex query you throw at them, but under the 
covers they may have to do a lot of expensive work.

Triggering expensive queries like this can quickly consume all the CPU and disk I/O on the machine which does not scale 
well for a many-user website. These queries may appear to execute quickly during development with small data volumes, 
but they become more expensive as the data grows. If your phone book has only 15 people, the query appears fast, but 
as you get to millions or billions of people it becomes extremely inefficient.

Because the database only supports a limit on the number of matches, not on the number of skipped rows or total rows 
scanned, we can’t limit the cost of such a query. Further, because queries are executed in a transaction, this query may cause a 
lot of locking and/or versioning work that slows down other queries that touch the same rows. It’s stressful to worry 
about query performance degradation, and it’s time consuming to monitor query performance in a large application.

Datarouter applications will generally have no long-running queries. While you can manually create secondary indexes in 
the database it can be tricky to make sure they are kept up to date in an evolving application. So far, Datarouter doesn't
focus on making it easy to trigger expensive queries, while it does focus on helping you design the application to scale 
from the beginning.

### How do we find rows without knowing the leftmost PK fields?
There are two ways:
- Stream all rows in the table back to the client
  - No extra code is needed
  - This will return rows in batches to the client where further filtering can happen in Java
  - Use this for infrequent queries that can wait for a potentially very slow response
  - Downside is that this method will consume a full CPU and a lot of disk I/O, so you should limit the number of these 
  *tablescans* operating on the database at any given time.
 - Add a secondary index
   - Use this for a fast B+ tree lookup in the secondary index
   - Create a special databean by subclassing `IndexEntry`. The schema-update utility will ensure that this index gets 
   created in the database and is used at runtime.
   - As a developer using the `IndexEntry` you’ll now have a clearer understanding of how much work you are asking the 
   database to do.
   - Downsides 
     - The index must also exist, consuming disk and memory
     - It must also be maintained causing updates to be slower

### Joins
Traditional SQL databases encourage you to create foreign keys between tables, letting you write queries that can fetch 
data from multiple tables and return a combined result set. The number of possible result set formats can grow large and 
is often handled with weakly typed objects like Lists of rows containing Maps to column values. Under the hood, the 
database is still accessing the same data pages as if you ran two selects and joined the data in Java. The more 
complicated the join, the more work the database must do.

A single master database is generally harder to scale than the application servers that use it, so it’s beneficial to 
move as much of the processing as possible to the application servers. With SQL it’s easy to add a few order-by clauses 
to the end of a query, but that sorting can be very expensive for the database to do. It can be better to pre-sort the 
data via primary and secondary indexes or to bring the data back to the scalable application tier before sorting.

All of the permutations of column selection, joins, filtering (skipping rows), and ordering lead to a large number of 
SQL statements that are generally harder to maintain that strongly-typed Java objects.

### Hibernate session cache
Traditional Java ORMs like Hibernate require that all database operations go through a *session cache* that holds a 
copy of all databeans involved with the session. It’s required so that the ORM is able to follow links between database 
and perform fancier features like updating all the relatives of a databean. It’s designed for operating on small numbers 
of databeans involved in a transaction and can become prohibitively slow when many databeans are touched within a 
session. Batch processing a large number of records can bring the session cache to a halt, forcing the application 
to take responsibility for invalidating the cache during processing. Session cache management complicates the application 
code in terms of clearing the cache, but more importantly in terms of knowing the consequences of clearing the cache in 
a complicated data model. Lazy-loading of a databean's relatives is also complex, leading to many unexpected slow queries 
in places you don’t expect them.

Datarouter loads and stores your data more explicitly and lets you hold onto a collection of databeans in the 
application if you choose, which is more predictable and debuggable and doesn’t tie your code to a specific ORM.

### Multiple databases
Traditional ORMs make the assumption that the application uses a single RDBMS. Many successful applications may start 
with a single RDBMS but soon find that they need to add slave databases for extra read capacity. To get further 
performance, they need to add a caching layer like memcached or redis and a messaging system like SQS. These caching and 
messaging layers don’t speak SQL, forcing the developers to write custom code for each cache scenario or use more complicated
ORM extensions or plugins. As data grows further, they need to move some large or frequently accessed tables to separate 
database machines. 

These slave databases, caches, messaging queues, and multiple databases break the original assumptions of the ORM. 
Hand-crafted SQL that joins two tables together must be rewritten into multiple queries so the join is done in the
application. The links between databeans that were configured into the data model (usually via annotations) must be 
removed, and code that relied on them must be modified.

A feature of the ORM is to make these links transparent to the application code, but this also makes them harder to 
remove as lazy-loading can happen in unexpected places outside of your DAO layer. Because the ORM does most of its work 
inside transactions, the application is using transactions everywhere even though many of them aren’t needed, so during 
this splitting of databases you must be careful to identify where transactions were actually necessary.

Datarouter advocates for assuming that all tables are on separate database servers from the start. Joins are done at the 
application layer, not caring which machines the data resides on or if those machines change. Transactions, where 
necessary, are explicitly coded into the application forcing you think ahead of time where they are necessary and making 
it easier to reason about which tables can be split apart. Datarouter provides a master/slave abstraction that can be 
inserted without changing business logic. Queries that are ok with stale data can pass a `slaveOk` parameter even if no 
slave databases exist yet. The caching layer is accessed with the same put, get, and delete operations as the RDBMS, 
making it easy to insert caching without changing the business logic.
