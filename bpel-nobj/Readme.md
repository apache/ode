OModel
=====
Basically, the OModel class uses a map as container of all fields,
and handles different serialization format\: java, json, smile. the latter
two are with help with jackson.

Migration and upgrade are also handled in this module. Testcases of
Serialization and migration are involved with compiler, so main testcases
are in bpel-compile module.

Serialization File Format
---------
New OModel has the same magic number and is different with old OModel.
Deserializer tell if it's old or new OModel by it's magic number. Magic
number and some other infomation are stored in file header. Serialized files
are as following:

```
 | Magic number  |
 |---------------|
 | other headers |
 |---------------|
 |    OProcess   |

```
The magic number can be used to distinct new and old omodel.
<code>Deserializer</code> is responsible for (de)serialize the while file.
Since old serialization format serialize magic number the same position as
we had here, <code>DeSerializer</code> can handle both old and new cbp files.

The new OModel support three format to (de)serialize OProcess. which are java,
json and smile. Corresponding <code>OmSerializer</code> and <code>OmDeserializer
</code> are implemented respectly.
format information are stored in file header. When deserialize, it will
seek for corresponding deserializer. File extensions are not used to
specify format. The default serialization format is java currently. Hope to
be configurable soon.

Migrations are done when deserialize Old omodel. upgrade to newest new omodel
are checked and done(if necessary) when deserialize. And if specified, the
upgraded OProcess will be writeback. (problems with the caller yet)
