package com.wieczorek.michal.webcompiler.webcompiler.service

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class FileCompilationPerformerService {

    fun performFileCompilation(sourceCode: String): String {
        val tempDir = Files.createTempDirectory("cpp_source")
        val sourceFile: Path = tempDir.resolve("source.cpp")
        Files.write(sourceFile, sourceCode.toByteArray())

        val outputFile: Path = tempDir.resolve("output")

        val compileCommand = arrayOf(
            "g++", sourceFile.toAbsolutePath().toString(), "-o", outputFile.toAbsolutePath().toString()
        )

        val compileProcess = ProcessBuilder(*compileCommand)
            .directory(tempDir.toFile())
            .redirectErrorStream(true)
            .start()
        val compileOutput = compileProcess.inputStream.bufferedReader().readText()
        val compileExitCode = compileProcess.waitFor()

        if (compileExitCode != 0) {
            return "Compilation failed:\n$compileOutput"
        }

        val runCommand = arrayOf(outputFile.toAbsolutePath().toString())
        val runProcess = ProcessBuilder(*runCommand)
            .directory(tempDir.toFile())
            .redirectErrorStream(true)
            .start()
        val runOutput = runProcess.inputStream.bufferedReader().readText()

        Files.deleteIfExists(sourceFile)
        Files.deleteIfExists(outputFile)
        Files.deleteIfExists(tempDir)

        return runOutput
    }
}