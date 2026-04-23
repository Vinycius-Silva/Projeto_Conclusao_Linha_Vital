package com.linhavital.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/teste")
class TestController {

    @GetMapping
    fun teste(): String {
        return "Backend funcionando 🚀"
    }
}