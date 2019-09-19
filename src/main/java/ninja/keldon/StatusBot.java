package ninja.keldon;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static ninja.keldon.BotMain.cfg;

public class StatusBot extends ListenerAdapter {
    public static final Logger LOGGER = LogManager.getLogger("main");
    public static String TOKEN = "";
    public static String COMMAND_PREFIX = "<";
    public static String SELF_ID = "";
    public static String DEF_STATUS = "";
    public static byte STATUS_TYPE = 0;
    public static boolean updatingStatus = true;

    public static HashMap<String, String> data = new HashMap<>();
    static {
        data.put("cmd_p", ";");
        data.put("use_id", "");
        data.put("def_status", "watching Anime");
    }
    private static JDA bot;
    void runBot() throws LoginException, InterruptedException {
        bot = new JDABuilder(AccountType.CLIENT)
                .setToken(TOKEN).build();
        bot.addEventListener(this);
        bot.getPresence().setGame(Game.watching("Anime"));
        bot.awaitReady();
        LOGGER.info("Bot has started.");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() && !event.getAuthor().getId().equals(SELF_ID)) return;
        if (event.getAuthor().getId().equals(SELF_ID)) {
            String raw = event.getMessage().getContentRaw();
            String stripped = raw.replaceFirst(COMMAND_PREFIX, "").trim();
            String command = stripped.split(" ")[0];
            String stripped2 = stripped.replaceFirst(command, "").trim();
            String[] args = stripped2.split(" ");
            if (command.equalsIgnoreCase("cmc")) {
                data.replace("cmd_p", args[0]);
                updateSettings();
                makeConfig();
                event.getMessage().delete().complete();
            } else if (command.equalsIgnoreCase("s")) {
                data.replace("def_status", stripped2);
                updateSettings();
                makeConfig();
                event.getMessage().delete().complete();
            }
            checkForUpdate();
        }
    }

    public static void checkForUpdate() {
        if (updatingStatus) {
            switch (STATUS_TYPE) {
                case 0: bot.getPresence().setGame(Game.playing(DEF_STATUS)); break;
                case 1: bot.getPresence().setGame(Game.watching(DEF_STATUS)); break;
                case 2: bot.getPresence().setGame(Game.listening(DEF_STATUS)); break;
                case 3: bot.getPresence().setGame(Game.streaming(DEF_STATUS, "")); break;
            }
            updatingStatus = false;
        }
    }

    public static void readConfig() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(cfg));
            String ln = "";
            while ((ln = reader.readLine()) != null) {
                String[] r = ln.split("==");
                data.replace(r[0], r[1]);
            }
            reader.close();
        } catch (Exception ignored) {}
        updateSettings();
    }

    public static void updateSettings() {
        data.forEach((k, v) -> {
            switch (k) {
                case "cmd_p": {
                    COMMAND_PREFIX = v;
                } break;
                case "use_id": {
                    SELF_ID = v;
                } break;
                case "def_status": {
                    switch (v.toLowerCase().charAt(0)) {
                        case 'w': STATUS_TYPE = 1; break;
                        case 'l': STATUS_TYPE = 2; break;
                        case 's': STATUS_TYPE = 3; break;
                        default: STATUS_TYPE = 0; break;
                    }
                    DEF_STATUS = v.replaceFirst(v.split(" ")[0], "").trim();
                } break;
            }
        });
        updatingStatus = true;
    }

    public static void makeConfig() {
        try {
            FileWriter writer = new FileWriter(cfg);
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String a = entry.getKey();
                String b = entry.getValue();
                writer.append(a).append("==").append(b).append(System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
