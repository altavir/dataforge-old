# Message input and output formats for Storages and Loaders # 

Any message is the DataForge Envelope with specific message code, Meta and possibly binary content. 
The message type by default equals standard DataForge message code (33 or '!'). 
Dataforge file storage type (43, '+') is used only for permanent file storages.
Message data type is usually ignored.

The envelope meta should contain following elements:

* `target` (node) - the storage or loader to which this message should be directed to. 
In case current `Responder` is not the target, it should redirect message to intended target. 
If it can not, `TargetNotFoundException` is thrown. 
* `source` (node) - optional parameter containing information about message source.
* `action` (string) - the action that should be performed. 
* `dataFormat` (node) - optional information about data format
* `data` (node) - optional node containing data.
 
## Target resolution ##

The target of message contains following elements:

* `name` (string) - the name of the target. In some cases chainpath could be used.
* `type` (string) - the type of the target. In some cases could be inferred and therefore optional.
* `meta` - optional additional metadata. For example loader metadata.

