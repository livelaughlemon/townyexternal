package me.orchidmc.townyexternal;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Objects;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

import org.bstats.bukkit.Metrics;

public final class TownyExternal extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig();
        FileConfiguration config = this.getConfig();

        int startPort = config.getInt("port");
        boolean townsEnabled = config.getBoolean("enable-towns");
        boolean nationsEnabled = config.getBoolean("enable-nations");
        boolean residentsEnabled = config.getBoolean("enable-residents");


        int pluginId = 18403;
        Metrics metrics = new Metrics(this, pluginId);

        // Plugin startup logic
        Bukkit.getLogger().info("[TownyExternal] Started. The External API is running on port " + startPort);
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(startPort), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", new CallingForNothing());

        if (townsEnabled) {
            // Town endpoints
            server.createContext("/town", new CallForTown());
            server.createContext("/towns", new CallForTownList());
            Bukkit.getLogger().info("[TownyExternal] Endpoints for towns enabled by config.");
        }
        if (nationsEnabled) {
            // Nation endpoints
            server.createContext("/nation", new CallForNation());
            server.createContext("/nations", new CallForNationList());
            Bukkit.getLogger().info("[TownyExternal] Endpoints for nations enabled by config.");
        }
        if (residentsEnabled) {
            // Resident endpoints
            server.createContext("/resident", new CallForResident());
            server.createContext("/residents", new CallForResidentList());
            Bukkit.getLogger().info("[TownyExternal] Endpoints for residents enabled by config.");
        }
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class CallingForNothing implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "{\"response\":\"200\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class CallForTown implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String townnamefromurl = t.getRequestURI().getPath();
            townnamefromurl = townnamefromurl.replace("/town", "");
            if (townnamefromurl.length() == 0 || townnamefromurl.equals("/")) {
                String response = "{\"response\":\"404\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                townnamefromurl = townnamefromurl.replace("/", "");
                Town town = TownyAPI.getInstance().getTown(townnamefromurl);
                assert town != null;

                Bukkit.getLogger().info("[TownyExternal] " + townnamefromurl + " was requested via the API.");

                if (town.hasNation()) {
                    String nationpart = "{\"name\":\""+ Objects.requireNonNull(town.getNationOrNull()).getName()+"\"}";
                    String response = "{\"response\":\"200\",\"name\":{\"raw\":\"" + town.getName() + "\",\"formatted\":\"" + town.getFormattedName() + "\"},\"board\":\"" + town.getBoard() + "\",\"tag\":\"" + town.getTag() + "\",\"founder\":\"" + town.getFounder() + "\",\"mayor\":\"" + town.getMayor() + "\",\"nation\":" + nationpart + ",\"residents\":\"" + town.getResidents() + "\",\"outlaws\":\"" + town.getOutlaws() + "\"}";

                    t.getResponseHeaders().set("Content-Type", "application/json");
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                } else {
                    String nationpart = "\"\"";
                    String response = "{\"response\":\"200\",\"name\":{\"raw\":\"" + town.getName() + "\",\"formatted\":\"" + town.getFormattedName() + "\"},\"board\":\"" + town.getBoard() + "\",\"tag\":\"" + town.getTag() + "\",\"founder\":\"" + town.getFounder() + "\",\"mayor\":\"" + town.getMayor() + "\",\"nation\":" + nationpart + ",\"residents\":\"" + town.getResidents() + "\",\"outlaws\":\"" + town.getOutlaws() + "\"}";

                    t.getResponseHeaders().set("Content-Type", "application/json");
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                }

            }
        }
    }

    static class CallForTownList implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            Bukkit.getLogger().info("[TownyExternal] List of all towns was requested via the API.");

            String response = "{\"response\":\"200\",\"towns\":\""+TownyAPI.getInstance().getTowns()+"\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

        }
    }

    static class CallForNation implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String nationnamefromurl = t.getRequestURI().getPath();
            nationnamefromurl = nationnamefromurl.replace("/nation", "");
            if (nationnamefromurl.length() == 0 || nationnamefromurl.equals("/")) {
                String response = "{\"response\":\"404\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                nationnamefromurl = nationnamefromurl.replace("/", "");
                Nation nation = TownyAPI.getInstance().getNation(nationnamefromurl);
                assert nation != null;

                Bukkit.getLogger().info("[TownyExternal] " + nationnamefromurl + " was requested via the API.");

                String response = "{\"response\":\"200\",\"name\":{\"raw\":\""+nation.getName()+"\",\"raw\":\""+nation.getFormattedName()+"\"},\"board\":\""+nation.getBoard()+"\",\"tag\":\""+nation.getTag()+"\",\"king\":\""+nation.getKing()+"\",\"assistants\":\""+nation.getAssistants()+"\",\"capital\":\""+nation.getCapital()+"\",\"towns\":\""+nation.getTowns()+"\",\"residents\":\""+nation.getResidents()+"\",\"allies\":\""+nation.getAllies()+"\",\"enemies\":\""+nation.getEnemies()+"\",\"outlaws\":\""+nation.getOutlaws()+"\"}";

                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();

            }
        }
    }

    static class CallForNationList implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            Bukkit.getLogger().info("[TownyExternal] List of all nations was requested via the API.");

            String response = "{\"response\":\"200\",\"nations\":\""+TownyAPI.getInstance().getNations()+"\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

        }
    }

    static class CallForResident implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String residentnamefromurl = t.getRequestURI().getPath();
            residentnamefromurl = residentnamefromurl.replace("/resident", "");
            if (residentnamefromurl.length() == 0 || residentnamefromurl.equals("/")) {
                String response = "{\"response\":\"404\"}";
                t.getResponseHeaders().set("Content-Type", "application/json");
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                residentnamefromurl = residentnamefromurl.replace("/", "");
                Resident resident = TownyAPI.getInstance().getResident(residentnamefromurl);
                assert resident != null;

                Bukkit.getLogger().info("[TownyExternal] " + residentnamefromurl + " was requested via the API.");

                if (resident.hasTown() && resident.hasNation()) {
                    String townname = resident.getTownOrNull().getName();
                    String nationname = resident.getNationOrNull().getName();
                    String response = "{\"response\":\"200\",\"name\":{\"raw\":\""+resident.getName()+"\",\"formatted\":\""+resident.getFormattedName()+"\"},\"town\":\""+townname+"\",\"nation\":\""+nationname+"\",\"title\":\""+resident.getTitle()+"\",\"prefix\":\""+resident.getNamePrefix()+"\",\"postfix\":\""+resident.getNamePostfix()+"\",\"friends\":\""+resident.getFriends()+"\"}";

                    t.getResponseHeaders().set("Content-Type", "application/json");
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else if (!resident.hasTown() && resident.hasNation()) {
                    String townname = "";
                    String nationname = resident.getNationOrNull().getName();
                    String response = "{\"response\":\"200\",\"name\":{\"raw\":\""+resident.getName()+"\",\"raw\":\""+resident.getFormattedName()+"\"},\"town\":\""+townname+"\",\"nation\":\""+nationname+"\",\"title\":\""+resident.getTitle()+"\",\"prefix\":\""+resident.getNamePrefix()+"\",\"postfix\":\""+resident.getNamePostfix()+"\",\"friends\":\""+resident.getFriends()+"\"}";

                    t.getResponseHeaders().set("Content-Type", "application/json");
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else if (resident.hasTown() && !resident.hasNation()) {
                    String townname = resident.getTownOrNull().getName();
                    String nationname = "";
                    String response = "{\"response\":\"200\",\"name\":{\"raw\":\""+resident.getName()+"\",\"raw\":\""+resident.getFormattedName()+"\"},\"town\":\""+townname+"\",\"nation\":\""+nationname+"\",\"title\":\""+resident.getTitle()+"\",\"prefix\":\""+resident.getNamePrefix()+"\",\"postfix\":\""+resident.getNamePostfix()+"\",\"friends\":\""+resident.getFriends()+"\"}";

                    t.getResponseHeaders().set("Content-Type", "application/json");
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else if (!resident.hasTown() && !resident.hasNation()) {
                    String townname = "";
                    String nationname = "";
                    String response = "{\"response\":\"200\",\"name\":{\"raw\":\""+resident.getName()+"\",\"raw\":\""+resident.getFormattedName()+"\"},\"town\":\""+townname+"\",\"nation\":\""+nationname+"\",\"title\":\""+resident.getTitle()+"\",\"prefix\":\""+resident.getNamePrefix()+"\",\"postfix\":\""+resident.getNamePostfix()+"\",\"friends\":\""+resident.getFriends()+"\"}";

                    t.getResponseHeaders().set("Content-Type", "application/json");
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }


            }
        }
    }

    static class CallForResidentList implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            Bukkit.getLogger().info("[TownyExternal] List of all residents was requested via the API.");

            String response = "{\"response\":\"200\",\"residents\":\""+TownyAPI.getInstance().getResidents()+"\"}";
            t.getResponseHeaders().set("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("[TownyExternal] Shutting down. Goodbye!");
    }

}

