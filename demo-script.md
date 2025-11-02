## Basic observability

Run the application in the sample folder using `quarkus dev`.

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

Open the config in the dev ui and fill in a model base URL, or uncomment the commented-config in application.properties.

It should switch to using an LLM to generate the creatures.

Good items for the todos:

- Sit down (the wuff should sit)
- Ghost (it should be a spectral stripe)
