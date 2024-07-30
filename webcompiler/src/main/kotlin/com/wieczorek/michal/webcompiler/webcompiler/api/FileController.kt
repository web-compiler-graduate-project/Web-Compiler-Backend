package com.wieczorek.michal.webcompiler.webcompiler.api

import com.wieczorek.michal.webcompiler.webcompiler.api.request.CompilationRequest
import com.wieczorek.michal.webcompiler.webcompiler.api.response.CompilationResponse
import com.wieczorek.michal.webcompiler.webcompiler.service.FileCompilationPerformerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class FileController(

    @Autowired val fileCompilationPerformerService: FileCompilationPerformerService

) {

    @PostMapping
    fun performFileCompilation(@RequestBody request: CompilationRequest): CompilationResponse {
        val compilationLogs = fileCompilationPerformerService.performFileCompilation(request.file)

        return CompilationResponse(output = compilationLogs)
    }
}