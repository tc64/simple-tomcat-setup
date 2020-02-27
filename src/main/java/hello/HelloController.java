package hello;

import org.springframework.web.bind.annotation.RestController;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {
    public static final Logger log = Logger.getLogger(HelloController.class.getName());
    
    @RequestMapping("/")
    public String index() {
    	//log.info("Inside hello controller...");
    	String test = "The movie was terrible, i would not recommend it to my worst enemy";
    	String res = CNNModel.doInference(test);
        return "inference done!";
    }
    
    @RequestMapping("/num_served")
    public int numServed() {
    	return CNNModel.numServed(); 
    }
    
    @RequestMapping("/last_phys_bytes")
    public long lastPhysBytes() {
    	return CNNModel.lastPhysBytes(); 
    }
    
    @RequestMapping("/highest_phys_bytes")
    public long highestPhysBytes() {
    	return CNNModel.highestPhysBytes(); 
    }
    
    @RequestMapping("/phys_bytes_now")
    public long physBytes() {
    	return CNNModel.physBytesNow(); 
    }
}
