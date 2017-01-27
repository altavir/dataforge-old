---
content_type: "doc_shard"
title: "Data format"
ordering: [100]
label: "data_format"
version: 1.0
date: 08.02.2016
published: true
---

The DataForge functionality is largely based on metadata exchange and therefore the main medium for messages between different parts of the system is `Meta` object and its derivatives. But sometimes one needs not only to transfer metadata but some binary or object data as well.

In order to do so one should use an `Envelope` format. It is a combined format for both text metadata and data in single block. An `Envelope` container consists of three main components:

1. **Properties**. A set of key-value bindings defining envelope format: metadata format, encoding and length, data format and length and general envelope version.
2. **Meta**. A text metadata in any supported format.
3. **Data**. Ant binary or textual data. The rules to read this data could be derived either from properties header or from envelope meta.