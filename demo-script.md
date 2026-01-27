# A quick demo

The key to a smooth demo is window management of the client.

## Start the client

```
cd modded-minecraft
./gradlew runClient
```

On mac, either tile the minecraft client by option dragging, and then
tile the browser, or make the client full screen and then use Mission Control to drag a browser window onto the client,
which will bring up split screen. Use another desktop for the IDE and dev UI.
If you forget, use `esc` to suspend the game while you arrange the window.

## Server

````shell
cd sample
quarkus dev
````

Click 'd' to open the dev UI. Visit the Grafana dashboard, and then visit the Micrometer Prometheus Registry.
Hit the main app a few times, to generate load.

## Adding Minecraft

But this isn't exciting enough!

Use option-drag to tile the minecraft app window, and also the browser window.
If you forget, use `esc` to suspend the game while you arrange the window.
Show that there is no server.

Comment out the lgtm code and run `quarkus ext add minecraft`.
Now the client can connect to a minecraft dev service.

Create an item. A chicken should appear in minecraft.

Create a duplicate item. That should trigger an exception.

Open the application properties and change the animal type.

## Adding AI

But the information we get about events is kind of limited. Can we do better?

Open the config in the dev ui and fill in a model base URL, or uncomment the commented-config in application.properties.

It should switch to using an LLM to generate the creatures.

Show how it changes in response to prompt (for example, "Sit down" as a todo makes a sitting wolf.)
Now we have information about user through the medium of creative wolf interpretation

Good items for the todos:

- Sit down (the wuff should sit)
- Ghost (it should be a spectral stripe)

## Extended option: Start with basic observability

Run the application in the sample folder using `quarkus dev`.

Click 'd' to open the dev UI. Visit the Grafana dashboard, and then visit the Micrometer Prometheus Registry.
Hit the main app a few times, to generate load.
