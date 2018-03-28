package hep.dataforge.workspace

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import java.util.function.Function

/**
 * Dynamic workspace that is parsed from file using external algorithm. Workspace is reloaded only if file is changed
 */
class FileBasedWorkspace(private val path: Path, private val parser: (Path) -> Workspace) : DynamicWorkspace() {
    private var checkSum: ByteArray? = null

    override val workspace: Workspace
        @Synchronized get() {
            val oldCheckSum = checkSum
            checkSum = getCheckSum()
            if (!Arrays.equals(oldCheckSum, checkSum)) {
                invalidate()
            }
            return super.workspace
        }

    override fun buildWorkspace(): Workspace {
        return parser(path)
    }


    private fun getCheckSum(): ByteArray {
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(Files.readAllBytes(path))
            return md.digest()
        } catch (ex: Exception) {
            throw RuntimeException("Failed to generate file checksum", ex)
        }

    }

    companion object {

        /**
         * Find appropriate parser and builder a workspace
         *
         * @param context        a parent context for workspace. Workspace usually creates its own context.
         * @param path           path of the file to create workspace from
         * @param transformation a finalization transformation applied to workspace after loading
         * @return
         */
        @JvmOverloads
        @JvmStatic
        fun build(context: Context, path: Path, transformation: Function<Workspace.Builder, Workspace> = Function { it.build() }): FileBasedWorkspace {
            val fileName = path.fileName.toString()
            return context.serviceStream(WorkspaceParser::class.java)
                    .filter { parser -> parser.listExtensions().stream().anyMatch { fileName.endsWith(it) } }
                    .findFirst()
                    .map { parser ->
                        FileBasedWorkspace(path) { p -> transformation.apply(parser.parse(context, p)) }
                    }
                    .orElseThrow { RuntimeException("Workspace parser for $path not found") }
        }

        fun build(path: Path): Workspace {
            return build(Global, path, Function { it.build() })
        }
    }
}