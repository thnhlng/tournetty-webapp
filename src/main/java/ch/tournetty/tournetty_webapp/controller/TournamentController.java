package ch.tournetty.tournetty_webapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tournament/api")
public class TournamentController {

    @GetMapping("hello")
    public String helloWorld() {
        return "Hello World";
    }

}
