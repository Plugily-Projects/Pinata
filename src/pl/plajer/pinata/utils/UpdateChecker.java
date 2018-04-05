package pl.plajer.pinata.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static String latestVersion;

    private static boolean checkHigher(String currentVersion, String newVersion) {
        String current = toReadable(currentVersion);
        String newVers = toReadable(newVersion);
        return current.compareTo(newVers) < 0;
    }

    public static UpdateType checkUpdate() {
        try {
            String currentVersion = "v" + Bukkit.getPluginManager().getPlugin("Pinata").getDescription().getVersion();
            String version = getVersion();
            if(checkHigher(currentVersion, version)) latestVersion = version;
            if(latestVersion != null) {
                if(latestVersion.contains("b")) {
                    return UpdateType.BETA;
                } else {
                    return UpdateType.STABLE;
                }
            }
            return UpdateType.UPDATED;
        } catch(Exception e) {
            return UpdateType.ERROR;
        }
    }

    public static String getLatestVersion() {
        return latestVersion;
    }

    private static String getVersion() {
        String version = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 46655).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.getOutputStream().write(("resource=" + 46655).getBytes("UTF-8"));
            version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return version;
    }

    private static String toReadable(String version) {
        String[] split = Pattern.compile(".", Pattern.LITERAL).split(version.replace("v", ""));
        version = "";
        for(String s : split)
            version += String.format("%4s", s);
        return version;
    }

    public enum UpdateType {
        STABLE, BETA, ERROR, UPDATED
    }

}
