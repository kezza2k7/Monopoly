package net.estopia;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Bot {
    public static void main(String[] args) throws LoginException {
        String token = "MTI1Mzc4NDk2NTEzNzU2Mzc2MA.GqD2kU.tU5sYLTKby9QSvOYf0fiq1ZOiieL3MCIFK-OWE";
        JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new GameEventHandler())
                .build();
    }
}