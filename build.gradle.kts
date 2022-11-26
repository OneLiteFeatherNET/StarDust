plugins {
    kotlin("jvm") version "1.7.10"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"

    id("com.github.johnrengelman.shadow") version "7.1.2"

    // SonarQube
    id("org.sonarqube") version "3.5.0.2730"
    jacoco
    // LIQUIBASE
    // alias(libs.plugins.liquibase)
}

val baseVersion = "1.0.0"
group = "net.onelitefeather"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.cloudnetservice.eu/repository/releases/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io")
}
dependencies {

    // Paper
    compileOnly(libs.paper)
    compileOnly(libs.luckperms)
    compileOnly(libs.protocollib)

    bukkitLibrary("dev.vankka:enhancedlegacytext:1.0.0")

    // Sentry
    implementation(libs.bundles.sentry)

    // Commands
    implementation(libs.bundles.cloud)
    implementation(libs.commodore) {
        isTransitive = false
    }

    // Database
    implementation("org.hibernate:hibernate-core:6.1.5.Final")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.6")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.1.5.Final")

    // liquibaseRuntime("org.liquibase.ext:liquibase-hibernate5:4.9.1") // Changelog based db
    // liquibaseRuntime("org.mariadb.jdbc:mariadb-java-client:3.0.4") // Changelog based db

    // Liquibase
    // liquibaseRuntime("org.liquibase:liquibase-core:3.10.3")
    // liquibaseRuntime("org.liquibase:liquibase-groovy-dsl:2.0.1")
    // liquibaseRuntime("ch.qos.logback:logback-core:1.2.3")
    // liquibaseRuntime("ch.qos.logback:logback-classic:1.2.3")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    getByName("sonarqube") {
        dependsOn(rootProject.tasks.test)
    }

    jacocoTestReport {
        dependsOn(rootProject.tasks.test)
        reports {
            xml.required.set(true)
        }
    }

    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.19.2")
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
}

bukkit {
    main = "${rootProject.group}.stardust.StardustPlugin"
    apiVersion = "1.19"
    name = "Stardust"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    authors = listOf("UniqueGame", "OneLiteFeather")
    softDepend = listOf("CloudNet-Bridge", "LuckPerms")
}

version = if (System.getenv().containsKey("CI")) {
    val releaseOrSnapshot = if (System.getenv("CI_COMMIT_BRANCH").equals("main", true)) {
        ""
    } else if(System.getenv("CI_COMMIT_BRANCH").equals("test", true)) {
        "-PREVIEW"
    } else {
        "-SNAPSHOT"
    }
    "$baseVersion$releaseOrSnapshot+${System.getenv("CI_COMMIT_SHORT_SHA")}"
} else {
    "$baseVersion-SNAPSHOT"
}

/*liquibase {
    activities {
        create("diffMain") {
            (this.arguments as MutableMap<String, String>).apply {
                this["changeLogFile"] = "src/main/resources/db/changelog/db.changelog-diff.xml"
                this["url"] = "jdbc:mariadb://localhost:3306/elytrarace"
                this["username"] = "root"
                this["password"] = "%Schueler90"
// set e.g. the Dev Database to perform diffs
                this["referenceUrl"] = "jdbc:mariadb://localhost:3306/elytraracediff"
                this["referenceUsername"] = "root"
                this["referencePassword"] = "%Schueler90"
            }
        }
    }
}*/
sonarqube {
    properties {
        property("sonar.projectKey", "onelitefeather_projects_stardust_AYRjNInxwVDHzVoeOyqT")
        property("sonar.qualitygate.wait", true)
    }
}

