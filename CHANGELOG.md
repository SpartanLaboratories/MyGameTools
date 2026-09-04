# Changelog

All notable changes to **GameTools** are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versions follow the project's `Major.Feature.MinorChange` scheme (see
[`.aiassistant/rules/CLAUDE.md`](.aiassistant/rules/CLAUDE.md) §6); a trailing letter marks a
bug-fix release. Releases are tagged `vX.Y.Z` and published to
[Maven Central](https://central.sonatype.com/artifact/io.github.spartanlaboratories/GameTools).

## [Unreleased]

### Changed
- **BREAKING — GameServer wire protocol (WebTools 2.0.0b).** Clients now send `Iam <name>`;
  the old trailing `<address>` token is accepted but ignored. The server's handshake reply
  is now a bare `TXRXON <sendPort> <receivePort>` (no leading address) sent back to the UDP
  source of the `Iam` datagram instead of `<clientAddress>:9999`. A client must therefore
  read the reply — and any common-channel broadcast — on the same socket it sent `Iam`
  from. `COMMON_SEND_PORT` is gone. This makes the handshake work through NAT.

### Dependencies
- WebTools `2.0.0` → `2.0.0b`; dropped the now-redundant `GeneralTools` exclude
  (WebTools 2.0.0b depends on `GeneralTools:2.0.1` directly).

## [1.9.0] — 2026-09-03

### Added
- **Capabilities on the `GameObject` tree.** `Capability` interface + `CoreCapability` enum
  (`MOVE`, `ATTACK`); each subtype declares `capabilities` (`Actor` adds `MOVE`, `Alive` adds
  `ATTACK`). `GameObject.can(capability)` reports whether one is usable right now, accounting
  for suppressing buffs. `Actor` gates movement on `can(MOVE)`; `Alive` gates its attack cycle
  on `can(ATTACK)`.
- **Buffs on the `GameObject` tree.** `Buff` (name, duration, stat mods, suppressed
  capabilities) with `onApplied` / `onTick` / `onExpired` hooks. `GameObject` gains `buffs`,
  `applyBuff`, `removeBuff`, `dispel`; `tick()` ages and reverts them.
- `Moddable` interface unifying `ModularStat` and `CombinedStat` as `StatMod` targets;
  `GameObject.stats` exposes stats by name.
- `BuffSnapshot` carried on `GameObjectSnapshot`, so active buffs reach every
  `DrawableSnapshot`.

### Changed
- `ModularStat.applyMod` / `removeMod` now return `Unit` (were `ModularStat`); no caller
  used the result.

## [1.8.0] — 2026-09-02

Pre-changelog release. See the [`v1.8.0`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.8.0)
tag.

## [1.7.0] — 2026-09-01

Pre-changelog release. See the [`v1.7.0`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.7.0) tag.

## [1.6.0] — 2026-08-30

Pre-changelog release. See the [`v1.6.0`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.6.0) tag.

## [1.5.2a] — 2026-08-30

Pre-changelog bug-fix release. See the [`v1.5.2a`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.5.2a) tag.

## [1.5.2] — 2026-08-30

Pre-changelog release. See the [`v1.5.2`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.5.2) tag.

## [1.5.1] — 2026-08-30

Pre-changelog release. See the [`v1.5.1`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.5.1) tag.

## [1.5.0] — 2026-08-30

Pre-changelog release. See the [`v1.5.0`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.5.0) tag.

## [1.4.0] — 2026-08-30

Pre-changelog release. First tagged release. See the [`v1.4.0`](https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.4.0) tag.

## [1.3.0] — 2026-08-30

Pre-changelog release (untagged). Actor angle/`Result` rework, `MouseAction` input routing,
`GameServer` implementation and networking tests.

[Unreleased]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.9.0...HEAD
[1.9.0]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.7.0...v1.8.0
[1.7.0]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.6.0...v1.7.0
[1.6.0]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.5.2a...v1.6.0
[1.5.2a]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.5.2...v1.5.2a
[1.5.2]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.5.1...v1.5.2
[1.5.1]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.5.0...v1.5.1
[1.5.0]: https://github.com/SpartanLaboratories/MyGameTools/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.4.0
[1.3.0]: https://github.com/SpartanLaboratories/MyGameTools/releases/tag/v1.4.0
