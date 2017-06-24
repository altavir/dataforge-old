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

* **Tag**. First 20 bytes of file or stream is reserved for envelope properties binary representation:
    1.	`#~` - two ASCII symbols, beginning of binary string.
    2.	4 bytes - properties `type` field: envelope format type and version. Depending on this value the rest of the binary string could be interpreted differently.
    3.  2 bytes - properties `metaType` field: metadata type.
    4.	4 bytes - properties `metaLength` field: metadata length in bytes including new lines and other separators.
    5.	4 bytes - properties `dataLength` field: the data length in bytes.
    6.	`~#` -  two ASCII symbols, end of binary string.
    7.	`\r\n` - two bytes, new line.

  The values are read as binary and transformed into 4-byte unsigned tag codes (Big endian).

* **Properties override**. Properties could be overridden with text values using following notation:

  `#? <property key> : <property value>; <new line>`

  Any whitespaces before `<property value>` begin are ignored. The `;` symbol is optional, but everything after it is ignored. Every property **must** be on a separate line. The end of line is defined by `\n` character so both Windows and Linux line endings are valid.

  Properties are accepted both in their textual representation or tag code.

* **Metadata block**. Metadata in any accepted format. Additional formats could be provided by modules. The default metadata format is *UTF-8* encoded *XML* (tag code 0x0000 or 0x584d). *JSON* format is provided by storage module.

  One must note that `metaLength` property is very important and in most cases is mandatory. It could be set to `0xffffffff` or `-1` value in order to force envelope reader to derive meta length automatically, but different readers do it in a different ways, so it strongly not recommended to do it if data block is not empty.

* **Data block**. Any other data. If `dataLength` property is set to `0xffffffff` or `-1`, then it is supposed that data block ends with the end of file or stream. Data block does not have any limitations for its content. It could even contain envelopes inside it!