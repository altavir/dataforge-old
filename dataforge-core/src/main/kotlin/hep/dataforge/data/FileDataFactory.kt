/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.data

import hep.dataforge.context.Context
import hep.dataforge.context.IOManager
import hep.dataforge.data.binary.Binary
import hep.dataforge.description.NodeDef
import hep.dataforge.description.NodeDefs
import hep.dataforge.kodex.toList
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.utils.NamingUtils.wildcardMatch
import hep.dataforge.workspace.FileReference
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@NodeDefs(
        NodeDef(name = "file", info = "File data element or list of files with the same meta defined by mask."),
        NodeDef(name = "dir", info = "Directory data node.")
)
class FileDataFactory : DataFactory<Binary>(Binary::class.java) {

    override val name: String="file"

    override fun fill(builder: DataNodeEditor<Binary>, context: Context, meta: Meta) {
        val parentFile: Path = when {
            meta.hasMeta(IOManager.DATA_DIRECTORY_CONTEXT_KEY) -> context.io.rootDir.resolve(meta.getString(IOManager.DATA_DIRECTORY_CONTEXT_KEY))
            else -> context.io.dataDir
        }

        /**
         * Add items matching specific file name. Not necessary one.
         */
        if (meta.hasMeta(FILE_NODE)) {
            meta.getMetaList(FILE_NODE).forEach { node -> addFile(context, builder, parentFile, node) }
        }

        /**
         * Add content of the directory
         */
        if (meta.hasMeta(DIRECTORY_NODE)) {
            meta.getMetaList(DIRECTORY_NODE).forEach { node -> addDir(context, builder, parentFile, node) }
        }

        if (meta.hasValue(FILE_NODE)) {
            val fileValue = meta.getValue(FILE_NODE)
            fileValue.getList().forEach { fileName ->
                addFile(context, builder, parentFile, MetaBuilder(FILE_NODE)
                        .putValue("path", fileName))
            }
        }
    }

    fun buildFileData(context: Context, filePath: String, meta: Meta): Data<Binary> {
        return buildFileData(context.io.getFile(filePath), meta)
    }

    private fun buildFileData(file: FileReference, meta: Meta): Data<Binary> {
        val mb = MetaBuilder(meta)
        mb.putValue(FILE_PATH_KEY, file.absolutePath.toString())
        mb.putValue(FILE_NAME_KEY, file.name)

        return DataUtils.readFile(file, mb)
    }

    /**
     * Add file or files providede via given meta to the tree
     * @param context
     * @param builder
     * @param parentFile
     * @param fileNode
     */
    private fun addFile(context: Context, builder: DataNodeEditor<Binary>, parentFile: Path, fileNode: Meta) {
        val files = listFiles(context, parentFile, fileNode)
        when {
            files.isEmpty() -> context.logger.warn("No files matching the filter: " + fileNode.toString())
            files.size == 1 -> {
                val file = FileReference.openFile(context, files[0])
                val fileMeta = fileNode.getMetaOrEmpty(DataFactory.NODE_META_KEY)
                builder.putData(file.name, buildFileData(file, fileMeta))
            }
            else -> files.forEach { path ->
                val file = FileReference.openFile(context, path)
                val fileMeta = fileNode.getMetaOrEmpty(DataFactory.NODE_META_KEY)
                builder.putData(file.name, buildFileData(file, fileMeta))
            }
        }
    }

    private fun listFiles(context: Context, oath: Path, fileNode: Meta): List<Path> {
        val mask = fileNode.getString("path")
        val parent = context.io.rootDir.resolve(oath)
        try {
            return Files.list(parent).filter { path -> wildcardMatch(mask, path.toString()) }.toList()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun addDir(context: Context, builder: DataNodeEditor<Binary>, parentFile: Path, dirNode: Meta) {
        val dirBuilder = DataTree.edit(Binary::class.java)
        val dir = parentFile.resolve(dirNode.getString("path"))
        if (!Files.isDirectory(dir)) {
            throw RuntimeException("The directory $dir does not exist")
        }
        dirBuilder.name = dirNode.getString(DataFactory.NODE_NAME_KEY, dirNode.name)
        if (dirNode.hasMeta(DataFactory.NODE_META_KEY)) {
            dirBuilder.meta = dirNode.getMeta(DataFactory.NODE_META_KEY)
        }

        val recurse = dirNode.getBoolean("recursive", true)

        try {
            Files.list(dir).forEach { path ->
                if (Files.isRegularFile(path)) {
                    val file = FileReference.openFile(context,path)
                    dirBuilder.putData(file.name, buildFileData(file, Meta.empty()))
                } else if (recurse && dir.fileName.toString() != META_DIRECTORY) {
                    addDir(context, dirBuilder, dir, Meta.empty())
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        builder.add(dirBuilder.build())

    }

    companion object {

        const val FILE_NODE = "file"
        const val FILE_MASK_NODE = "files"
        const val DIRECTORY_NODE = "dir"

        const val FILE_NAME_KEY = "fileName"
        const val FILE_PATH_KEY = "filePath"

        const val META_DIRECTORY = "@meta"
    }

}
