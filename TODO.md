# TODO

_[see our Milestones](https://github.com/OASIS-learn-study/minecraft-storeys-maker/milestones), and_
_[see done](done.md) for what's already finished._

## Next Steps

- [ ] add `DynamicAction` to `docs/testing.md` using `dynamic-test.story`
- [ ] allow `javax.script` "local JS" instead of remote Vert.x API in `config/storeys-web/scripts`
- [ ] extend JS script (`dynamic-test.story` or JS) with custom command registration
- [ ] sandbox "local JS" scripts so that they can't do `java.lang.System.exit(-1)` and what not

- [ ] **fix #310 for correct _unregistering_** (for Scratch + `DynamicAction` scripts)
- [ ] **make Scratch server-side only** (autosaved; subset of #52)

- [ ] switch JS in-process scripts from Nashorn (?) to GraalVM's `javax.script` JSR-223 implementation (#295)
- [ ] make Scratch server-side use our own Minecraft Java/JS API, instead of Vert.x Event Bus

- [ ] Java API simple example (in-process not remote, and fixed not hot-reloading, yet)
- [ ] Java API with hot-reloading
- [ ] make `SeleniumTest` either do something useful again (test the JS API, without any Scratch), or.. remove it now?! (Shame.)
- [ ] code Java & JS in Codespace and continously git live sync into container? Using (something like) https://github.com/tkellogg/dura
- [ ] switch from Scratch to Blockly? (#264)

## Docs

- [ ] add an architecture.md in docs/
- [ ] much simplify the README

## Ops

- [ ] LuckPerms + MagiBridge #276

## Bugs

- [ ] fix ugly LF seen in title of `test.story`
- [ ] print error to user of `/story` (only) when entity to narrate wasn't found
- [ ] story editor in Book
- [ ] /wpm command, to set per-Player ReadingSpeed
- [ ] /move to make entites move around instead of /tp .. slowly, step by step - timed (based on WPM)
- [ ] Entity velocity 0 via script or new /noai (?) instead of in NarrateAction
- [ ] /story:stop
- [ ] /narrate @p | @MsPiggy bla bla bla
- [ ] /tp @pig: In Parser, or in Sponge, so it also works in console?
- [ ] interactive, prompts
- [ ] sub story modules (includes, label welcome: and ->welcome or #welcome ?)
- [ ] Like "Conversations", for learning? (learn.study)
- [ ] /story name completion
- [ ] record and store real path of walking around
- [ ] parallelism ({}), if needed?  Through indention..
- [ ] look at other story telling frameworks: interactive fiction engines, GitHub topics storytelling, story, stories,

## Tech

- [ ] refactoring out a /dsl/ module for `.story`
- [ ] add reporting to https://bstats.org
- [ ] api artifact with some services interfaces, for other mods
- [ ] webserver, with tokens and REST API support incl. OpenAPI
- [ ] web-based editor, simple
- [ ] live collab web editor
- [ ] WARN on console if narrating entity is not in view of player (as that can be confusing)
- [ ] Use Immutables.org for model; just gen. hashCode/equals & toString or real immutable with *Builder for each?
- [ ] ActionParserTest using xtendbeans
- [ ] Human, with skin (https://github.com/SpongePowered/SpongeCommon/issues/318)
- [ ] Action execute methods in separate class(es) for pure model?

## Chat / Narrate / Banner Text

- [ ] Inline http[s]:// links should be click-able
- [ ] MAYBE allow [Google](https://www.google.com) - but can be risky  better to show link - or ask players to confirm?
- [ ] MD Text _emphasis (also *emphasis*) and **strong** (see https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet#emphasis)
- [ ] NLS: hello: [en] howdy [fr] salut.  #hello @p
- [ ] Full FormattingCodeTextSerializer ?
- [ ] Emoji? :sparkles: :camel: :boom: :+1: (see https://www.webpagefx.com/tools/emoji-cheat-sheet/)
- [ ] paged chat messages

## World Distribution

- [ ] https://github.com/SpongePowered/Schematic-Specification
- [ ] world plugin which can load previously saved (parts of SMALL) world dumps
- [ ] script to create the demo world with Piggy & Chestnut: /summon, /name etc. (How to fence, leash?)
- [ ] script gen. from existing world?!

## User Support & Marketing

- [ ] add more videos to https://www.youtube.com/playlist?list=PL7PA3zq_6Oqce-C2MhAK4FWb98OTFVrQo
- [ ] Forum?
