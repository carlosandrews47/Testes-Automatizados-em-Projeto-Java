package meusistema.demo.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String tratarErroGeral(
            Exception ex,
            Model model) {

        model.addAttribute(
                "erro",
                ex.getMessage());

        return "login";
    }
}