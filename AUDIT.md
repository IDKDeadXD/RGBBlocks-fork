# RGBBlocks 1.21.1 Audit

Audit date: 2026-09-17

Repository audited: local `RGBBlocks-fork` repository on branch `1.21.1`.

## Confirmed Bugs

### Paint Bucket could crash with non-player use contexts

- Description: `PaintBucketItem.useOn` checked `context.getPlayer()` for sneak-copy, then dereferenced `context.getPlayer().isCreative()` in the paint path without proving a player existed.
- Root cause: the paint path assumed every `UseOnContext` was backed by a real player.
- Affected code: `PaintBucketItem`.
- Fix implemented: the player is captured once, null-checked, and durability is only consumed when a real non-creative player is present. Painting still works through the server-side block entity path.

### RGB colors had inconsistent normalization and missing defaults could become black

- Description: different lifecycle paths used `-1`, `0`, or raw arbitrary ints as color values. Missing block-entity color NBT loaded as `0`, which rendered black.
- Root cause: there was no central color sanitizer/default, and `CompoundTag.getInt("color")` silently returned `0` when the key was absent.
- Affected code: `Color`, `RGBBlockEntity`, `RGBFallingBlockEntity`, color handlers, crafting, dispenser painting, FramedBlocks camo.
- Fix implemented: `Color.DEFAULT_RGB`, `Color.sanitizeRGB`, hex formatting, and component helpers now centralize opaque RGB handling. Missing color defaults to white, matching the old `-1` item default.

### Block entity component loading did not refresh derived map color

- Description: `RGBBlockEntity.applyImplicitComponents` assigned `this.color` directly, bypassing `mapColor` recalculation.
- Root cause: component loading did not use the same setter as NBT loading and painting.
- Affected code: `RGBBlockEntity`.
- Fix implemented: component application now routes through `setColor`.

### Color updates over-invalidated blocks

- Description: painting and packet handling used `Block.UPDATE_ALL_IMMEDIATE`.
- Root cause: color-only changes were treated like full immediate block updates.
- Affected code: `RGBBlockEntity`, `PaintBucketItem`, `DispensePaintBucketBehaviour`, concrete powder landing.
- Fix implemented: color changes use `setColorAndSync`, which sends `Block.UPDATE_CLIENTS` only when the color actually changes.

### Command/tool-created block color data was too narrow

- Description: block entity loading only understood numeric `color`. Command-created blocks using `rgb` style data or hex strings would not resolve to the intended color.
- Root cause: no tolerant parsing for command or external-copy NBT shapes.
- Affected code: `RGBBlockEntity`, `Color`.
- Fix implemented: loading accepts canonical numeric `color`, hex string `color`, `hex`, `rgb` int arrays/lists, and `red`/`green`/`blue` numeric components. The mod still saves canonical numeric `color`.

### Paint Bucket color-copy behavior was not discoverable

- Description: sneak-use copy existed but had no tooltip.
- Root cause: missing localization and tooltip text.
- Affected code: `PaintBucketItem`, `ModLanguageProvider`, generated `en_us.json`.
- Fix implemented: added `tooltip.rgbblocks.paint_bucket.copy`.

### Common tooltip/TOP code referenced client GUI classes for shared constants

- Description: common item and TOP compatibility code imported `ColorSelectScreen` or `ClientUtils` just to format tooltip values or open the GUI.
- Root cause: UI constants lived only on a client screen class.
- Affected code: `PaintBucketItem`, `RGBBlockItem`, `RGBBlockProvider`, `Color`.
- Fix implemented: shared HSB constants moved to `Color`; common item tooltips no longer directly import client GUI classes. Opening the color screen is isolated to the client-side call path.

### NeoGradle run configuration used deprecated APIs

- Description: every Gradle run printed `Run.getProgramArguments()` deprecation warnings.
- Root cause: `build.gradle` used `programArgument` / `programArguments`.
- Affected code: `build.gradle`.
- Fix implemented: server and data runs now use `arguments`.

## Historical Issues Investigated

### #17: command usage / command-created RGB blocks black

- Still applies partially to 1.21.1 before this pass.
- Findings: current block entities understood numeric `color` only. Missing or differently shaped NBT fell back through `getInt` to black/default behavior.
- Resolution: added tolerant NBT parsing for numeric color, hex strings, RGB arrays/lists, and red/green/blue components.

### #16: Forge 43 expected on MC 1.19.x

- Obsolete for this branch.
- Findings: 1.21.1 targets NeoForge `21.1.73` and Java 21.
- Resolution: no code change.

### #15: Advanced Colored Lighting support

- Still a feature request, not a stabilization bug.
- Findings: no Iris/Oculus/ACL integration exists in this codebase.
- Resolution: left for a later shader-specific feature pass.

### #14: textures not loading

- Likely addressed before this pass by the existing virtual-pack cache work in history.
- Findings: current resources and datagen completed successfully; generated models point to RGBBlocks textures.
- Resolution: no additional code change in this pass.

### #13: borderless antiblock

- Feature request.
- Findings: out of scope for stabilization.
- Resolution: no code change.

### #10: WorldEdit usage

- Still a compatibility limitation.
- Findings: generic block-entity NBT parsing is now more tolerant, which helps command/copy tools that preserve block entity data. WorldEdit-specific integration was not added.
- Resolution: partial RGBBlocks-side improvement only.

### #8: tessellation crash after middle-clicking painted slab

- Likely mitigated by current item color/data hardening.
- Findings: slab pick-block delegates to `RGBBlockUtils.getCloneItemStack`, which copies the color component when a block entity exists. Item color handlers now tolerate missing or malformed color data.
- Resolution: no direct repro was performed; no crash-specific stack trace was available.

### #7 and #5: Rubidium/RubidiumExtra color display/desaturation

- Could not be confirmed on 1.21.1 NeoForge from code alone.
- Findings: color handlers now consistently return sanitized opaque RGB. No Sodium/Rubidium runtime was tested.
- Resolution: no renderer-specific compatibility code added.

### #6: RGB glass pane transparency

- Could not be visually reproduced in this pass.
- Findings: 1.21.1 generated glass and pane models declare `minecraft:translucent`; pane templates are symmetric side/alt templates. Existing glass blocks use skylight propagation and full shade brightness.
- Resolution: documented as needing visual/client verification with a real scene.

### #4: WorldEdit/Create/Building Gadgets/Construction Wand compatibility

- Still partially applicable.
- Findings: tools that copy block state only cannot represent arbitrary RGB because color lives in block entity/component data. Tools that preserve block entity NBT or item components should benefit from the new tolerant color parsing.
- Resolution: generic lifecycle fix only; no hard dependencies added.

### #3 and #2: WallBlocks / Chisel and Bits compatibility

- Feature requests or cross-mod integration requests.
- Findings: not solvable from RGBBlocks alone without designing new integrations.
- Resolution: no code change.

### #1: server connection NPE

- Obsolete historical 1.16.5 report.
- Findings: current dedicated server startup reached `Done` in the 1.21.1 dev runtime.
- Resolution: no issue-specific code change.

## Rendering Compatibility Pass

### Sources checked on 2026-09-17

- Sodium: Modrinth lists current client-side Fabric/NeoForge/Quilt support and a NeoForge `mc1.21.1-0.8.13-neoforge` release for Minecraft 1.21.1.
  - https://modrinth.com/mod/sodium
  - https://modrinth.com/mod/sodium/version/mc1.21.1-0.8.13-neoforge
- Iris: Modrinth lists current client-side Fabric/NeoForge/Quilt support and a NeoForge `1.8.12+1.21.1-neoforge` release for Minecraft 1.21.1 that requires Sodium.
  - https://modrinth.com/mod/iris
  - https://modrinth.com/mod/iris/version/1.8.12%2B1.21.1-neoforge
- Embeddium: Modrinth lists client-side NeoForge support for Minecraft 1.21.1, including `1.0.15+mc1.21.1`, and documents it as a Sodium-derived renderer with extra mod-compatibility APIs and optional translucency sorting.
  - https://modrinth.com/mod/embeddium
  - https://modrinth.com/mod/embeddium/version/1.0.15%2Bmc1.21.1
- Oculus: Modrinth currently lists Forge/NeoForge support only through Minecraft 1.20.1, so it is not a primary 1.21.1 NeoForge target for this fork.
  - https://modrinth.com/mod/oculus
- Complementary Reimagined and Photon both target Iris/OptiFine shader loading and list colored-lighting features. Photon documents voxel-based colored lighting as Iris-only, and Complementary's r5.2.2 changelog introduced Advanced Colored Lighting through Iris.
  - https://modrinth.com/shader/complementary-reimagined
  - https://modrinth.com/shader/complementary-unbound/version/r5.2.2
  - https://modrinth.com/shader/photon-shader
- `eclipseisoffline/iris-coloured-lights` is a shader library, not a NeoForge block API. It maps shader `block.properties` IDs to GLSL light colors and requires SSBO-capable shader support.
  - https://github.com/eclipseisoffline/iris-coloured-lights

### Local renderer API findings

- NeoForge 21.1.73 still resolves block model `"render_type": "minecraft:translucent"` through `NamedRenderTypeManager` to the translucent block render type and layered translucent item render type.
- Vanilla/NeoForge `ItemBlockRenderTypes` marks direct render-layer registration as deprecated; the recommended 1.21 path is model JSON `render_type` or `BakedModel#getRenderTypes`.
- RGBBlocks generated models already use `minecraft:solid` for opaque blocks, `minecraft:cutout` for antiblock, and `minecraft:translucent` for glass/glass slabs/glass stairs/glass panes. `AntiblockBakedModel#getRenderTypes` delegates to the baked base model.
- Vanilla `ModelBlockRenderer` asks `BlockColors` for tint values at the rendered block position. Terrain break particles also query block colors at their source position, while falling dust calls `FallingBlock#getDustColor`.
- Client `sendBlockUpdated` routes through `LevelRenderer.blockChanged`; client `setBlocksDirty` routes through `LevelRenderer.setBlockDirty`. The existing `Block.UPDATE_CLIENTS` color sync remains the right low-noise chunk repaint trigger for color-only block entity updates.

### Code changes

- Removed the generic `RGBBlockColor` fallback from `pos` to `pos.below()`. That fallback could let a renderer tint one RGB block from a different block entity directly below it, especially in optimized chunk builders that rely on vanilla block-color contracts.
- Kept the offset behavior constrained to `RGBConcretePowderBlock#getDustColor`, where vanilla actually asks the block for falling-dust color. The method now tries the actual dust position first, then the pre-existing one-block-up lookup, and sanitizes the returned RGB.
- No Sodium, Iris, Embeddium, or Oculus APIs were hard-linked. The compatibility strategy remains vanilla/NeoForge render contracts: block/item color handlers, model JSON render types, `BakedModel#getRenderTypes`, and normal client block updates.

### Advanced Colored Lighting conclusion

- RGBBlocks can render arbitrary per-block RGB tint because that color is stored in block entity/component data and exposed through vanilla color handlers.
- Minecraft's normal block light value is still scalar brightness, not dynamic per-block RGB light. Current Iris/ACL-style shader solutions map fixed block IDs to shader-side colors through shaderpack data, not arbitrary runtime block entity colors.
- True dynamic RGB light emission for this mod would require a separate shader/Iris integration design that exports RGBBlocks block positions and colors to shader-accessible data. That is a feature project, not a renderer compatibility bug fix.

## Compatibility Findings

### NeoForge / Minecraft 1.21.1

- The branch builds against Minecraft `1.21.1`, NeoForge `21.1.73`, and Java 21 toolchains.
- Gradle build and datagen are reproducible from repository-managed dependencies via CurseMaven.

### Dedicated server

- `runServer` reached `Done` with RGBBlocks, FramedBlocks, and The One Probe present in the dev runtime.
- The console did not accept `stop` through the automation PTY, so the process was interrupted after successful startup. This means startup was verified, but graceful console shutdown was not.

### FramedBlocks

- Existing integration is present under `util.compat.framedblocks`.
- The dev runtime loaded FramedBlocks `10.2.1`.
- Camo color storage now sanitizes colors consistently, and paint-bucket copy/paint paths preserve exact RGB values through the camo container code.
- Full in-game camo application, reload, break, and multiplayer behavior was not manually tested.

### The One Probe

- Existing integration is present under `util.compat.top`.
- The dev runtime loaded TOP `1.21_neo-12.0.5`.
- TOP color display no longer imports the client color screen for HSB constants.
- In-game probe overlay rendering was not manually tested.

### Rendering

- Block and item color handlers now sanitize missing/malformed data instead of returning raw component values.
- Glass, glass slab, glass stair, and glass pane models use generated `minecraft:translucent` render types.
- `RGBBlockColor` now tints strictly from the block entity at the queried render position.
- Concrete powder falling-dust color preserves its localized one-block-up fallback for vanilla dust particle positioning.

### Building/copy tools

- RGBBlocks cannot make block-state-only tools preserve arbitrary colors because the color is not a block state property.
- Tools that preserve block entity NBT or item components now have safer supported input forms.

## Remaining Limitations

- No live client gameplay pass was performed.
- No two-player multiplayer synchronization pass was performed.
- No Sodium, Iris, Embeddium, Oculus, or shader-pack runtime stack was installed or visually tested.
- No WorldEdit, Building Gadgets, Construction Wand, Create, Chisel and Bits, or Structurize runtime tests were performed.
- FramedBlocks and TOP were startup/datagen validated in the dev runtime, but their full user workflows were not manually exercised.
- Advanced Colored Lighting / true dynamic RGB light emission remains a separate shader integration feature request.
- No unit tests were added because the rendering-sensitive behavior depends on Minecraft client render and particle integration. This fork currently has no test source set; Gradle still reports `test` and `testJunit` as `NO-SOURCE`.

## Testing Performed

- `gradlew.bat clean build` passed after the first stabilization commit.
- `gradlew.bat spotlessApply build` passed after command NBT parsing changes.
- `gradlew.bat runData` passed and wrote deterministic generated language/cache output.
- `gradlew.bat build` passed after the NeoGradle run-argument cleanup, with the prior deprecation warning removed.
- `gradlew.bat runServer` reached `Done` on a dedicated server dev runtime with RGBBlocks, FramedBlocks, and TOP loaded. The process was interrupted after startup because stdin did not deliver `stop` to the server console.
- `gradlew.bat clean build` passed before the rendering compatibility pass.
- `gradlew.bat spotlessApply build` passed after constraining render tint lookups.

## Commits Created

- `94fca09 Stabilize RGB color data flow`
- `39a8fbd Accept command RGB color NBT`
- `5d61038 Refresh generated language data`
- `450ed1d Update NeoGradle run arguments`
- `74811a7 Constrain RGB tint lookups`
