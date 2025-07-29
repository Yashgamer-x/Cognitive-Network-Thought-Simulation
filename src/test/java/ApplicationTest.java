import com.yashgamerx.cognitive_thought_network_simulation.storage.MySQLDatabase;
import org.junit.jupiter.api.Test;

public class ApplicationTest {

    @Test
    public void testJDBC(){
        var connection = MySQLDatabase.getInstance().getConnection();
        assert connection != null;
    }
}
