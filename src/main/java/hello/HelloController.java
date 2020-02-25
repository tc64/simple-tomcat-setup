package hello;

import org.springframework.web.bind.annotation.RestController;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {
    public static final Logger log = Logger.getLogger(HelloController.class.getName());
    
    
    @RequestMapping("/")
    public String index() {
    	log.info("Inside hello controller...");
        return "Greetings from Spring Boot!";
    }
    
    
    
}
