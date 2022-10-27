# Contributing to AudiTranscribe

## Introduction

First off, thank you for considering contributing to AudiTranscribe. AudiTranscribe is a small project, and it's people
like you that make it such a great tool.

Following these guidelines communicates that you respect the time of the developers managing and developing this open
source project. In return, they should reciprocate that respect in addressing your issue, assessing changes, and helping
you finalize your pull requests.

## What To Contribute

There are many ways to contribute to AudiTranscribe. There are three **big** types of contributions we are looking our
for:

- Improving the documentation
- Improving the design of the application
- Fixing bugs **OR** improving efficiency of the program

In addition, feature requests and bug reports are greatly appreciated. They help make AudiTranscribe a better tool and
more stable for users to use.

## Your First Contribution

Unsure how to begin? There are a few places you can find issues for the project.

- The [AudiTranscribe Bug Tracking Sheet](https://github.com/orgs/AudiTranscribe/projects/2). This neatly organizes all
  the bugs that are found, and that need fixing.
- The [Issue Tracker](https://github.com/AudiTranscribe/AudiTranscribe/issues). This is the main way of obtaining the
  issues that we need help with. If you're looking for something to contribute, try:
    - Beginner issues: Issues that should only require a few lines of code, and a test or two
    - Help Wanted issues: Issues that are more involved
- The [AudiTranscribe Feature Plan](https://github.com/orgs/AudiTranscribe/projects/3). This contains all planned
  features for future releases.

## Submitting Contributions

For something that is bigger than a one or two line fix:

1. Create your own fork of the code
2. Do the changes in your fork
3. If you like the change and think the project could use it, send a pull request!

However, for small issues such as

- spelling / grammar fixes
- typo correction, white space and formatting changes
- comment clean up

you could submit an issue instead of a pull request.

**Note:**

- (Development) changes to *AudiTranscribe* should be pushed to the `main` branch**. The `staging`
  branch should be reserved for production builds. Never push any changes to the `staging` branch.
- Make pull requests instead of directly editing files on the `main` branch.

**Important:** Please follow the [Commit Message Format](#appendix-commit-message-format) to make commits to
AudiTranscribe.

## Reporting Bugs

If you find a bug within AudiTranscribe, please
[submit an issue](https://github.com/AudiTranscribe/AudiTranscribe/issues) on the issue tracker. We recommend using the
issue template to aid us in fixing the bug.

## Suggesting Features/Enhancements

Features and enhancements are welcome! We are always looking for ways to improve AudiTranscribe and to make it a better
application for everyone to use.

We have a philosophy for what we do in AudiTranscribe:

- We believe that AudiTranscribe should be easy to use and easy to install.
- It should not be cumbersome for new users to install AudiTranscribe.
- Code written in AudiTranscribe should be easy to read, easy to understand, and easy to amend.

If you find yourself wishing for a feature that doesn't exist in AudiTranscribe, you are probably not alone. There are
probably many others who have thought of adding such a feature into AudiTranscribe. Open an issue on our issues list on
which describes the feature you would like to see, why you need it, and how it should work.

## Appendix: Commit Message Format

*This specification is inspired by
the [AngularJS commit message format](https://docs.google.com/document/d/1QrDFcIiPjSLDn3EL15IJygNPiHORgU1_OOAqWjiDU5Y).*

We have very precise rules over how our Git commit messages must be formatted. This format makes it **easier to read the
commit history**.

Each commit message consists of a **header**, a **body**, and a **footer**.

```
<header>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The `header` is mandatory and must conform to the [Commit Message Header](#commit-message-header) format.

The `body` is mandatory for all commits *except for those of type "docs"*. When the body is present it must conform to
the [Commit Message Body](#commit-message-body) format.

The `footer` is optional. The [Commit Message Footer](#commit-message-footer) format describes what the footer is used
for and the structure it must have.

#### Commit Message Header

```
<type>(<scope>): <short summary>
  │       │             │
  │       │             └─⫸ Summary in present tense. No period at the end.
  │       │
  │       └─⫸ Commit Scope: Section of code that the change applies to.
  │
  └─⫸ Commit Type: build|ci|docs|feat|fix|perf|refactor|test
```

The `<type>` and `<short summary>` fields are mandatory. The `(<scope>)` field is optional.

- The type specifies the type of commit that is being made.<br>
  The type must be one of the following:
    - **`build`**: Changes that affect the build system or external dependencies.
    - **`ci`**: Changes to the CI configuration files and scripts.
    - **`docs`**: Documentation only changes.
    - **`feat`**: A new feature.
    - **`fix`**: A bug fix *for a previous version*.
    - **`perf`**: A code change that improves performance.
    - **`refactor`**: A code change that neither fixes a bug nor adds a feature.
    - **`test`**: Add missing tests or correcting existing tests.
- The scope should be the section of code that the change applies to (for example, `transcription` for the transcription
  view).
- The summary field is used to provide a succinct description of the change.
    - Use the imperative, present tense: "change" not "changed" nor "changes"
    - No dot (`.`) at the end

#### Commit Message Body

Just as in the summary, use the imperative, present tense: "fix" not "fixed" nor "fixes".

Explain the motivation for the change in the commit message body. This commit message should explain *why* you are
making the change. You can include a comparison of the previous behavior with the new behavior in order to illustrate
the impact of the change.

#### Commit Message Footer

The footer can contain information about breaking changes and deprecations and is also the place to reference GitHub
issues, Jira tickets, and other PRs that this commit closes or is related to.
For example:

```
BREAKING CHANGE: <breaking change summary>              ⫷───── Header
<BLANK LINE>
<breaking change description + migration instructions>  ⫷───── Body
<BLANK LINE>
Fixes #<issue number>                                   ⫷───── Footer
```

or

```
DEPRECATED: <what is deprecated>                        ⫷───── Header
<BLANK LINE>
<deprecation description + recommended update path>     ⫷───── Body
<BLANK LINE>
Closes #<pr number>                                     ⫷───── Footer
```

Breaking Change section should start with the phrase "BREAKING CHANGE: " followed by a summary of the breaking change, a
blank line, and a detailed description of the breaking change that also includes migration instructions.

Similarly, a Deprecation section should start with "DEPRECATED: " followed by a short description of what is deprecated,
a blank line, and a detailed description of the deprecation that also mentions the recommended update path.

### Revert commits

If the commit reverts a previous commit, it should begin with `revert: `, followed by the header of the reverted commit.

The content of the commit message body should contain:

- Information about the SHA of the commit being reverted in the following format: `This reverts commit <SHA>`.
- A clear description of the reason for reverting the commit message.
