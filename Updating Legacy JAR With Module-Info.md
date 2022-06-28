# Updating Legacy JAR With a `module-info.java` File

Adapted from the comment by Philip Guin on [StackOverflow](https://stackoverflow.com/a/47222302).

---
We would hope that all dependencies already have a `module-info.java` file. However, some do not. In these cases, you
*will* need to update them. In the case where an error like "automatic module cannot be used with jlink" occurs, this
file is meant to summarize what to do.

Here's a list of all the dependencies that need updating, with any issues that you might encounter when updating them.

| Dependency Name         | Module Name                   | Issues Associated With It <u>To Ignore</u>      |
|:------------------------|:------------------------------|:------------------------------------------------|
| Apache Commons Compress | `org.apache.commons.compress` | Missing dependencies error                      |
| Apache Commons Exec     | `commons.exec`                |                                                 |
| Javatuples              | `javatuples`                  |                                                 |
| SQLite JDBC             | `org.xerial.sqlitejdbc`       |                                                 |

Suppose we want to fix the Commons Compress (`org.apache.commons.compress`) package.
In the error message where you see "automatic module cannot be used with jlink", the error message
should also specify a file path. For example:

```
Error: automatic module cannot be used with jlink: org.apache.commons.compress from file:/path/to/the/jar/file
```

Follow these steps to fix this issue.

## Step 1: `cd /directory/that/contains/the/jar/file`

For example:

```bash
cd ~/.m2/repository/org/apache/commons/commons-compress/1.21/
```

## Step 2: `jdeps --generate-module-info . <jar_file>`

For example:

```bash
jdeps --generate-module-info . commons-compress-1.21.jar
```

If dependencies are missing, run `jdeps --ignore-missing-deps --generate-module-info . <jar_file>`. For example:

```bash
jdeps --ignore-missing-deps --generate-module-info . commons-compress-1.21.jar
```

Take note of the output of the command. It should start with `writing to` and end with `module-info.java` For
example, `writing to ./org.apache.commons.compress/module-info.java`. The directory *before* the `module-info.java` is
the **module name**.

## Step 3: `javac --patch-module <module_name>=<jar_file> <module_name>/module-info.java`

For example:

```bash
javac --patch-module org.apache.commons.compress=commons-compress-1.21.jar org.apache.commons.compress/module-info.java
```

## Step 4: `jar uf <jar_file> -C <module_name> module-info.class`

For example:

```bash
jar uf commons-compress-1.21.jar -C org.apache.commons.compress module-info.class
```

## You're done!

You should now have a module that is no longer automatic, and have a `module-info.java` file. JLink should now work as
expected.
