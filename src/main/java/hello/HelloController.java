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
    	String test = "The movie was terrible, i would not recommend it to my worst enemy";
    	String res = CNNModel.doInference(test);
        return "inference done!";
    }
}
