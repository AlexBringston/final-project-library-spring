package ua.training.controllers;

import lombok.extern.log4j.Log4j2;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Log4j2
public class ErrorController {

    @ExceptionHandler(RuntimeException.class)
    public String getErrorPage(RuntimeException exception, Model model){
        log.error("Exception " + exception.getClass() + " was handled");
        model.addAttribute("message", exception.getMessage());
        return "message";
    }
}
