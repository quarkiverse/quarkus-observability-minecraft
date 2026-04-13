package com.example.examplemod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.jetbrains.annotations.NotNull;

@Path("/observability")
public class Endpoint {

    // Ugly and static, but it will work for the moment
    private static Object player;

    @POST
    @Path("/log")
    @Consumes("text/plain")
    public void log(String message) {
        System.out.println("[Quarkcraft] log");
        invokeOnPlayer("say", message, null);

    }

    @POST
    @Path("/event-with-details")
    @Consumes("application/json")
    //    @Produces("text/plain")
    public String alert(String config) {
        System.out.println("[Quarkcraft] " + config);
        String name = invokeOnPlayer("customEvent", "A thing happened out in the real world",
                config);
        return name;

    }

    @POST
    @Path("/event")
    @Consumes("text/plain")
    public String simpleAlert(String type) {
        System.out.println("[Quarkcraft] event" + type);
        return invokeOnPlayer("event", "A thing happened out in the real world", type);

    }

    @POST
    @Path("/boom")
    public String explode() {
        System.out.println("[Quarkcraft] boom");
        return invokeOnPlayer("explode", "Something -bad- happened out in the real world", null);
    }

    @POST
    @Path("/set-respawn")
    public String setRespawn() {
        System.out.println("[Quarkcraft] set-respawn");
        return invokeOnPlayer("setRespawn", "Respawn point updated", null);
    }

    @POST
    @Path("/kill")
    public String kill() {
        System.out.println("[Quarkcraft] kill");
        return invokeOnPlayer("killPlayer", "Killing player to trigger respawn", null);
    }

    @POST
    @Path("/respawn")
    public String respawn() {
        System.out.println("[Quarkcraft] respawn");
        return invokeOnPlayer("respawnPlayer", "Respawning player", null);
    }

    @NotNull
    private String invokeOnPlayer(String methodName, String message, String param) {
        if (player != null) {
            // The player may be in a different classloader to us, so we need to use more reflection
            try {
                // Cheerfully assume all methods on PlayerWrapper take a string as an argument
                Method m = player.getClass()
                        .getMethod(methodName, String.class, String.class);
                m.invoke(player, message, param);
                return "minecraft world updated with " + methodName;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return "internal error";
            }
        } else {
            return "no player logged in";
        }
    }

    public static void setPlayer(Object newPlayer) {
        player = newPlayer;
    }

}
