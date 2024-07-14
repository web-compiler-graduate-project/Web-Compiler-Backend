package com.wieczorek.michal.webcompiler.webcompiler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class WebcompilerApplication

fun main(args: Array<String>) {
	runApplication<WebcompilerApplication>(*args)
}
