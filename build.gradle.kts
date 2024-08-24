plugins {
    kotlin("jvm") version "2.0.0"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("de.chojo.publishdata") version "1.4.0"
    `maven-publish`
    // SonarQube
//    id("org.sonarqube") version "4.2.1.3168"
//    jacoco
}

val baseVersion = "1.1.0"
group = "net.onelitefeather"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
}
dependencies {

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    implementation("cloud.commandframework", "cloud-paper", "1.8.2")
    implementation("cloud.commandframework", "cloud-annotations", "1.8.2")
    implementation("cloud.commandframework", "cloud-minecraft-extras", "1.8.4")
    implementation("org.apache.commons:commons-lang3:3.16.0")
    implementation("me.lucko:commodore:2.2") {
        isTransitive = false
    }

    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")

    // Database
    implementation("org.hibernate:hibernate-core:6.1.5.Final")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.1.5.Final")

    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.2")
    implementation("org.postgresql:postgresql:42.7.3")



    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {

//    getByName("sonar") {
//        dependsOn(rootProject.tasks.test)
//    }
//
//    jacocoTestReport {
//        dependsOn(rootProject.tasks.test)
//        reports {
//            xml.required.set(true)
//        }
//    }

    runServer {
        minecraftVersion("1.20.6")
        jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
    }

    test {
        useJUnitPlatform()

    }

    shadowJar {
        archiveFileName.set("${rootProject.name}.${archiveExtension.getOrElse("jar")}")
    }
}

paper {
    main = "${rootProject.group}.stardust.StardustPlugin"
    apiVersion = "1.19"
    name = "Stardust"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    authors = listOf("UniqueGame", "OneLiteFeather")
    serverDependencies {
        register("CloudNet-Bridge") {
            required = false
        }
        register("LuckPerms") {
            required = false
        }
        register("ProtocolLib") {
            required = false
        }
    }
    // softDepend = listOf("CloudNet-Bridge", "LuckPerms", "ProtocolLib")
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


publishData {
    addBuildData()
    useGitlabReposForProject("78", "https://gitlab.onelitefeather.dev/")
    publishTask("shadowJar")
}


publishing {
    publications.create<MavenPublication>("maven") {
        // configure the publication as defined previously.
        publishData.configurePublication(this)
        version = publishData.getVersion(false)
    }

    repositories {
        maven {
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }


            name = "Gitlab"
            // Get the detected repository from the publish data
            url = uri(publishData.getRepository())
        }
    }
}
