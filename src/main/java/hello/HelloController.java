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
    	String test = "With \"Batman Returns\", Tim Burton succumbed to an important priority in American cinema: giving a sequel to a blockbuster. Three years after the most successful movie of the year, \"Batman\" (1989), here comes the hero of Gotham City again for the pleasure of many spectators. Like its predecessor, \"Batman Returns\" enjoyed an enormous commercial success. Tim Burton made Batman come back once again because it is to believe that the victory of the latter on his enemy the Joker did little to improve the image of Gotham City. Indeed, violence and corruption still exist and here, the second word is epitomized by Max Shreck (Christopher Walken) and the penguin (Danny De Vito). These two crooks who are eager to take control of Gotham City are going to make life difficult for Batman. His task will be more difficult with the apparition of Catwoman... <br /><br />Tim Burton still delivered much work on the scenery and it seems that he tried to correct the faults of the first movie. It means that the director gave way to fight sequences which are better mastered than in the first Batman. But he didn't neglect the psychology of his main characters for all that. If in the movie of 1989, Burton had focused on Bruce Wayne/Batman's psychology, here, it's the penguin that obviously interested him. The director attempted to make of him, very well acted by De Vito an ambiguous character. On one hand, he's full of bad intentions (previously quoted) but on the other hand, he's searching for his past and would like to be considered as a real man.<br /><br />\"Batman Returns\" also appears as crazier than its predecessor, especially with the cast. I think of Michèle Pfeiffer who offers a daring and powerful performance as Catwoman. Besides, at the time when I'm writing this review, it makes me remember that a new version of Catwoman is currently at the cinema. But given the bad reviews (3 out of 10 on IMDb!), it is sure that her performer Halle Berry must pale into insignificance beside Pfeiffer.<br /><br />Ultimately, this film is one of the best sequels of 1992 and it enables to develop Tim Burton's peculiar style. We also still find his taste for the strange and the Gothic. Concerning Batman's other adaptations for the screen, you can skip them. \"Batman Forever\" (1995) and \"Batman and Robin\" (1997) are no good. I heard that Christopher Nolan was shooting the beginning of the Batman story and it was to be released next year. Will it match Tim Burton's works?";
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
    
    @RequestMapping("/total_bytes_now")
    public long totalBytes() {
    	return CNNModel.totalBytesNow(); 
    }
    
    @RequestMapping("/periodic_gc_on")
    public String pdGcOn() {
    	CNNModel.turnOnPeriodicGc();
    	return ""; 
    }
    
    @RequestMapping("/periodic_gc_off")
    public String pdGcOff() {
    	CNNModel.turnOffPeriodicGc();
    	return "";
    }
}
