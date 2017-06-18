---
content_type: "doc_shard"
title: "Envelope format"
chapter: envelope
ordering: 1
label: "envelope_format"
version: 1.0
date: 08.02.2016
published: true
---

An envelope is a logical structure and its physical binary representation could be different for different purposes, but the default for envelope files or streams is the following:

* **Tag**. First 30 bytes of file or stream is reserved for envelope properties binary representation:
    1.	`#!` - two ASCII symbols, beginning of binary string.
    2.	4 bytes - properties `type` field: envelope format type and version. Depending on this value the rest of the binary string could be interpreted differently.
    3.	4 bytes - currently reserved.
    4.  4 bytes - properties `metaType` field: metadata type and encoding.
    5.	4 bytes - properties `metaLength` field: metadata length in bytes including new lines and other separators.
    6.	4 bytes - properties `dataType` field: data format and type. This field is not necessary for some applications and could be used for other purposes.
    7.	4 bytes - properties `dataLength` field: the data length in bytes.
    8.	`!#` -  two ASCII symbols, end of binary string.
    9.	`\r\n` - two bytes, new line.

  The values are read as binary and transformed into 4-byte unsigned tag codes.

* **Properties override**. Properties could be overridden with text values using following notation:

  `#? <property key> : <property value>; <new line>`

  Any whitespaces before `<property value>` begin are ignored. The `;` symbol is optional, but everything after it is ignored. Every property **must** be on a separate line. It is recommended to use universal new line sequence `\r\n` to ensure correct work on any system.

  Properties are accepted both in their textual representation or tag code.

* **Metadata block**. Metadata in any accepted format. Additional formats could be provided by modules. The default metadata format is *UTF-8* encoded *XML* (tag code 0x00000000). *JSON* format is provided by storage module.

  One must note that `metaLength` property is very important and in most cases is mandatory. It could be set to `0xffffffff` or `-1` value in order to force envelope reader to derive meta length automatically, but different readers do it in a different ways, so it strongly not recommended to do it if data block is not empty.

* **Data block**. Any other data. If `dataLength` property is set to `0xffffffff` or `-1`, then it is supposed that data block ends with the end of file or stream. Data block does not have any limitations for its content. It could even contain envelopes inside it!