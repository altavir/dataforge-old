---
content_type: "doc_shard"
title: "Storage plugin"
ordering: [1000]
label: "storage"
version: 1.0
date: 08.02.2016
published: true
---
Storage plugin defines an interface between DataForge and different data storage systems such as databases, remote servers or other means to save and load data.

The main object in storage system is called `Storage`. It represents a connection to some data storing back-end. In terms of SQL databases (which are not used by  DataForge by default) it is equivalent of database. `Storage` could provide different `Loaders`. A `Loader` governs direct data pushing and pulling. In terms of SQL it is equivalent of table.

---

**Note**: DataForge storage system is designed to be used with experimental data and therfore loaders optimized to put data online and then analyze it. Operations to modify existing data are not supported by basic loaders.

---

Storage system is hierarchical: each storage could have any number of child storages. So ot is basically a tree. Each child storage has a reference for its parent. The sotrage without a parent is called root storage. The system could support any number of root storages at a time using `storage` context plugin.

By default DataForge storage module supports following loader types:

1. **PointLoader**. Direct equivalent of SQL table. It can push or pull `DataPoint` objects. `PointLoader` contains information about `DataPoint` `DataFormat`. It is assumed that this format is just a minimum requirement for `DataPoint` pushing, but implementation can just cut all fields tht are not contained in loader format.
2. **EventLoader**. Can push DataForge events.
3. **StateLoader**. The only loader that allows to change data. It holds a set of key-value pairs. Each subsequent push overrides appropriate state.
4. **BinaryLoader**. A named set of `fragment` objects. Type of these objects is defined by generic and API does not define the format or procedure to read or right these objects.

Loaders as well as Storages implement `Responder` interface and and could accept requests in form of [envelopes](#envelope_format).