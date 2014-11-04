Rangers
=======

A hunter-hunted Minecraft gamemode

Instructions for Building with Apache Maven
-------------------------------------------
1. Clone a copy of this git repository (`git clone https://github.com/coasterman10/Rangers.git`)
2. `cd` into the project root directory
3. Add `spigot1543.jar` to the Maven local repository (`mvn install:install-file -Dfile=lib/spigot1543.jar -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=1543 -Dpackaging=jar`)
4. Compile the project (`mvn clean compile jar`)
5. The plugin jar can be found under the `target` directory

Instructions for Building with Gradle
-------------------------------------
1. Clone a copy of this git repository (`git clone https://github.com/coasterman10/Rangers.git`)
2. `cd` into the project root directory
3. Build the project (`gradle build`)
4. The plugin jar can be found under the `build/libs` directory
+ In order to get the current versions of the project, simply run `gradle getVersion`
