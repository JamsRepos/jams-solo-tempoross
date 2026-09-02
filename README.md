# Jam's Solo Tempoross

A click-here helper for the **16/19/19** solo Tempoross rotation (west boat, north island). It highlights what to click next, draws a path there, and recovers if you AFK, miss a wave, or dump the wrong amount of fish.

This plugin does not click for you.

## What you see

- **Click highlight** — the fishing spot, shrine, totem, crate, pump, or spirit pool you should click now is outlined. While you are already acting, the following destination can be outlined and labelled Next.
- **Path** — a line on the floor and/or minimap to where you are going. Fires are pathed around. Turn either line off in settings if you prefer.
- **Status panel** — the current step, fish counts, buckets, and (optionally) a compact energy / storm / points readout instead of the default game HUD.
- **Deposit countdown** — a large on-screen count of crate deposits left, with optional audio chimes at 3, 2, 1, and a stop at 0. The stop can also play when other AFK steps finish.
- **Recover:** — if you fall off the rotation, the panel says so and points you back.

After an update, chat shows what changed the first time you log in.

## The 16/19/19 rotation

Solo-start, then:

1. Fill empty buckets at the ship pump and run north.
2. Fish until 8, cook, click the double spot when it appears, fish until 19, cook all.
3. Tether the totem when a colossal wave closes in.
4. Deposit **16** cooked fish (keep 3).
5. Douse 3–4 fires.
6. Fish 16 more (doubles first), cook, deposit all 19.
7. Harpoon the spirit pool until energy is back to 100%.
8. Fish and cook 19, deposit 19, spirit pool to 100%. Repeat once more, then spirit pool until Tempoross dies.
9. Refill buckets at the dock tap, Leave, Solo-start the next game.

## What to bring

- A **harpoon** (equipped or in inventory)
- A **rope**, or the full **Spirit Angler** outfit
- A **hammer**, or an **Imcando hammer**
- **4 buckets** (empty or water)

## Settings

Everything is on by default except the idle screen tint.

| Setting | What it does |
|---|---|
| Enable helper | Turns the highlights, path, and panel on or off |
| Click highlight | This click, this click & next, next only, or off |
| Path display | Where the path is drawn: floor & minimap, floor only, minimap only, or off |
| Path source | Plugin lines, or hand the destination to the [Shortest Path](https://runelite.net/plugin-hub/show/shortest-path) plugin coloured by the current step |
| Show status panel | Step, counts, and reminders |
| Deposit countdown | Large on-screen count of crate deposits left |
| Countdown size | Text size when the countdown reaches the last 3 deposits |
| Replace game HUD | Hide the default Tempoross bars; show energy, storm, and points in the status panel |
| Hide fishing and cooking overlays | Hide RuneLite's fishing/cooking stat overlays during a game |
| Countdown chime | Audio at 3, 2, 1 deposits left, then a bell at 0 |
| Final chime | All Actions also bells when fishing, cooking, filling, or the spirit pool finishes. Deposit Only keeps the bell on the crate countdown |
| Sound volume | Plugin chime volume (does not change the game's sound-effect slider) |
| Idle reminder | How long you can stand still before a warning |
| Idle screen tint | Optional faint tint when idle |

---

## Development

Requires JDK 11+.

```bash
./gradlew test        # unit tests
./gradlew shadowJar   # build plugin jar
./gradlew run         # launch dev client
```

For Jagex accounts, follow [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

## License

BSD-2-Clause. See [LICENSE](LICENSE).
