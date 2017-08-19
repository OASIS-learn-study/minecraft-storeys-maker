
[X] Name Tag read/write
[X] scroll text in name tag
[X] Splitter fully TDD implemented
[X] mv ch.vorburger.minecraft.storeys.narrate to ch.vorburger.minecraft.storeys
[X] story file and parser
[X] Story =Header= and ==Subhead==
[X] Speed of story - correctly use ReadingSpeed in each Action (with ActionWaitHelper)
[X] /narrate -> /story (StoryCommand)
[ ] Refactor to remove .narrate. from packages, NarratorPlugin -> StoreysPlugin, id = storeys
[X] Bug "more than 1 entity"
[X] Normal (non-OSGi) mod (but still work under OSGi as well, for dev)
[ ] Latest Sponge version
[ ] README & Video (by D?)
[ ] CI (incl. also building our deps)
[ ] Forum
[ ] LICENSE-HEADER

[ ] Like "Conversations", for learning? (learn.study)

[ ] wait (pause) in story
[ ] /wpm command, to set per-Player ReadingSpeed
[ ] /move to make entites move around instead of /tp .. slowly, step by step - timed (based on WPM)
[ ] Entity velocity 0 via script or new /noai (?) instead of in NarrateAction
[ ] /story:stop
[ ] /narrate @p | @MsPiggy bla bla bla
[ ] /tp @pig: In Parser, or in Sponge, so it also works in console?
[ ] interactive, prompts
[ ] sub story modules (includes, label welcome: and ->welcome or #welcome ?)
[ ] api artifact with some services interfaces, for other mods

[ ] story editor in Book
[ ] webserver, with tokens and REST API support incl. OpenAPI
[ ] web-based editor, simple
[ ] live collab web editor

[ ] /story name completion
[ ] record and store real path of walking around
[ ] WARN on console if narrating entity is not in view of player (as that can be confusing)
[ ] Use Immutables.org for model; just gen. hashCode/equals & toString or real immutable with *Builder for each?
[ ] ActionParserTest using xtendbeans
[ ] parallelism ({}), if needed?  Through indention..
[ ] Human, with skin (https://github.com/SpongePowered/SpongeCommon/issues/318)
[ ] Action execute methods in separate class(es) for pure model?

[ ] Inline http[s]:// links should be click-able
[ ] MAYBE allow [Google](https://www.google.com) - but can be risky  better to show link - or ask players to confirm?
[ ] MD Text _emphasis (also *emphasis*) and **strong** (see https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet#emphasis)
[ ] NLS: hello: [en] howdy [fr] salut.  #hello @p
[ ] Full FormattingCodeTextSerializer ?
[ ] Emoji? :sparkles: :camel: :boom: :+1: (see https://www.webpagefx.com/tools/emoji-cheat-sheet/)
[ ] paged chat messages

[ ] world plugin which can load previously saved (parts of SMALL) world dumps
[ ] script to create the demo world with Piggy & Chestnut: /summon, /name etc. (How to fence, leash?)
[ ] script gen. from existing world?!
[ ] Bundle hello.story into JAR and extract on start, if not present

[ ] look at other story telling frameworks: interactive fiction engines, GitHub topics storytelling, story, stories,
