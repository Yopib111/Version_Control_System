package svcs

import java.io.File
import java.security.MessageDigest


fun main(args: Array<String>) {
//fun main() {
//    val args = arrayOf("checkout", "95bcaa510e4cf8ecfb1075d0a4eaf66f95247a00232b80eeeed5e29eb415d65e")
    val configFile = File("vcs\\config.txt") //C:\Users\user\IdeaProjects\Version Control System\
    val indexFile = File("vcs\\index.txt") //C:\Users\user\IdeaProjects\Version Control System\
    val logFile = File("vcs\\log.txt")
    val commandsList = mapOf<String, String>(
        "--help" to """
            These are SVCS commands:
            config     Get and set a username.
            add        Add a file to the index.
            log        Show commit logs.
            commit     Save changes.
            checkout   Restore a file.
        """.trimIndent(),
        "config" to "Get and set a username.",
        "add" to "Add a file to the index.",
        "log" to "Show commit logs.",
        "commit" to "Save changes.",
        "checkout" to "Restore a file."
    )
    val commitIDList = mutableSetOf<String>()

    createFolder()
    createCommitFolder()
    createConfigFile(configFile)
    createIndexFile(indexFile)
    createLogFile(logFile)


    if (args.isEmpty()) println(commandsList["--help"])
    else {
//    for (i in args) {
        when (args[0]) {
            "--help", "" -> println(commandsList["--help"])
            "config" -> {
                if (args.size == 1) {
                    if (configFile.readText().isEmpty())
                        println("Please, tell me who you are.")
                    else println("The username is ${configFile.readText()}.")
                } else {
                    configFile.writeText(args[1])
                    println("The username is ${configFile.readText()}.")
                }
            }

            "add" -> {
                if (args.size == 1) {
                    if (indexFile.readText().isEmpty())
                        println("Add a file to the index.")
                    else println("Tracked files:\n${indexFile.readText()}")
                } else {
                    val addingFile = File(args[1])
                    if (addingFile.exists()) {
                        indexFile.appendText("${args[1]}\n")
                        println("The file '${args[1]}' is tracked.")
                    } else println("Can't find '${args[1]}'.")
                }
            }

            "log" -> if (logFile.readText().isEmpty()) println("No commits yet.")
            else {
                println(logFile.readText())
            }
            "commit" -> {
                if (args.size == 2) {
                    val commitID = buildHashID(indexFile)
                    if (logFile.readText().isNotEmpty() && commitID == logFile.readLines().first().substringAfter(' ')) {
                        println("Nothing to commit.")
                    } else {
                        addLog("""
                        commit $commitID
                        Author: ${configFile.readText()}
                        ${args[1]}
                    """.trimIndent()
                        )
                        println("Changes are committed.")
                        val commitDir = File("vcs\\commits\\$commitID")
                        commitDir.mkdir()
                        for (line in indexFile.readLines()) {
                            File(line).copyTo(File("vcs\\commits\\$commitID\\$line"))
                        }
                    }
                } else {
                    println("Message was not passed.")
                }
            }

            "checkout" -> {
                if (args.size == 1) println("Commit id was not passed.")
                else {
                    var checkingStatus = false
                    for (lines in logFile.readLines()) {
                        if (args[1] in lines) {
                            checkout(args[1], indexFile)
                            checkingStatus = true
                            println("Switched to commit ${args[1]}.")
                        }
                    }
                    if (!checkingStatus) println("Commit does not exist.")
                }
            }
            else -> println("'${args[0]}' is not a SVCS command.")
//        }
        }
    }
}

fun createFolder() {
    val dir = File("vcs")
    if (!dir.exists()) dir.mkdir()
}

fun createCommitFolder() {
    val dir = File("vcs\\commits")
    if (!dir.exists()) dir.mkdir()
}

fun createConfigFile(creatingFile: File) {
    if (!creatingFile.exists()) creatingFile.writeText("")
}

fun createIndexFile(creatingFile: File) {
    if (!creatingFile.exists()) creatingFile.writeText("")
}

fun createLogFile(creatingFile: File) {
    if (!creatingFile.exists()) creatingFile.writeText("")
}

fun buildHashID(indexFile: File): String {
    var input = ""
    for (lines in indexFile.readLines()) {
        val path = File(lines)
        input += path.readText()
    }
    val bytes = input.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun addLog(str: String){
    val logFile = File("vcs\\log.txt")
    val oldText = logFile.readText()
    logFile.writeText(str)
    logFile.appendText("\n\n$oldText")

}

fun checkout(args:String, indexFile: File) {
    for (files in indexFile.readLines()) {
        val fileName = File("vcs\\commits\\${args}\\${files}")
        val fileContent = fileName.readText()
        val newFile = File(files)
        newFile.writeText(fileContent)
    }
}