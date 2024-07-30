package com.wieczorek.michal.webcompiler.webcompiler.service

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class FileCompilationPerformerService {

    fun performFileCompilation(sourceCode: String): String {
        // Utwórz tymczasowy plik na kod źródłowy
        val tempDir = Files.createTempDirectory("cpp_source")
        val sourceFile: Path = tempDir.resolve("source.cpp")
        Files.write(sourceFile, sourceCode.toByteArray())

        // Ścieżka do pliku wyjściowego (skompilowanego programu)
        val outputFile: Path = tempDir.resolve("output")

        // Komenda kompilacji C++
        val compileCommand = arrayOf(
            "g++", sourceFile.toAbsolutePath().toString(), "-o", outputFile.toAbsolutePath().toString()
        )

        // Uruchom kompilację
        val compileProcess = ProcessBuilder(*compileCommand)
            .directory(tempDir.toFile())
            .redirectErrorStream(true)
            .start()
        val compileOutput = compileProcess.inputStream.bufferedReader().readText()
        val compileExitCode = compileProcess.waitFor()

        // Sprawdź, czy kompilacja zakończyła się sukcesem
        if (compileExitCode != 0) {
            // Jeśli nie, zwróć logi kompilacji jako błąd
            return "Compilation failed:\n$compileOutput"
        }

        // Jeśli kompilacja się powiodła, uruchom skompilowany program
        val runCommand = arrayOf(outputFile.toAbsolutePath().toString())
        val runProcess = ProcessBuilder(*runCommand)
            .directory(tempDir.toFile())
            .redirectErrorStream(true)
            .start()
        val runOutput = runProcess.inputStream.bufferedReader().readText()

        // Usuń tymczasowe pliki
        Files.deleteIfExists(sourceFile)
        Files.deleteIfExists(outputFile)
        Files.deleteIfExists(tempDir)

        return runOutput
    }
}