package ninja.keldon;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BotMain {
    public static final File tkn = new File("./dis.tkn");
    public static final File cfg = new File("./bot.cfg");
    public static void main(String[] args) throws LoginException, InterruptedException {
        try {
            if (!tkn.exists()) {
                tkn.createNewFile();
                System.out.println("Must have your token in ./dis.tkn");
                System.exit(-666);
            } else {
                BufferedReader tknr = new BufferedReader(new FileReader(tkn));
                String tok = tknr.readLine();
                if (tok.isEmpty()) {
                    System.out.println("Must have your token in ./dis.tkn");
                    System.exit(-666);
                } else {
                    StatusBot.TOKEN = tok;
                }
            }
            if (!cfg.exists()) {
                cfg.createNewFile();
                StatusBot.makeConfig();
            }
            StatusBot.readConfig();
        } catch (Exception ignored) {}
        new StatusBot().runBot();
    }
}
