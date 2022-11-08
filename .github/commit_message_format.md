# Commit Message Format

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

## Commit Message Header

```
<type>(<scope>): <short summary>
  │       │             │
  │       │             └─⫸ Summary in present tense. No period at the end.
  │       │                  Do not capitalise first letter of first word.
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
    - Do not capitalise first letter of first word

## Commit Message Body

Just as in the summary, use the imperative, present tense: "fix" not "fixed" nor "fixes".

Explain the motivation for the change in the commit message body. This commit message should explain *why* you are
making the change. You can include a comparison of the previous behavior with the new behavior in order to illustrate
the impact of the change.

## Commit Message Footer

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

## Reverting commits

If the commit reverts a previous commit, it should begin with `revert: `, followed by the header of the reverted commit.

The content of the commit message body should contain:

- Information about the SHA of the commit being reverted in the following format: `This reverts commit <SHA>`.
- A clear description of the reason for reverting the commit message.
