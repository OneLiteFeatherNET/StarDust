plugins {
    kotlin("jvm") version "2.0.0"
    alias(libs.plugins.pluginYaml)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runServer)
    alias(libs.plugins.publishData)
    `maven-publish`
}

version = "1.3.0"
group = "net.onelitefeather"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
}
dependencies {

    compileOnly(libs.paper)
    implementation(libs.bundles.cloud)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC.2")
    compileOnly(libs.luckperms)
    compileOnly(libs.protocolLib)

    implementation(libs.bundles.hibernate)
    implementation(libs.jaxbRuntime)
    implementation(libs.postgresql)
    implementation(libs.apacheCommons)

    // Testing
    testImplementation(libs.junitApi)
    testRuntimeOnly(libs.junitEngine)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {

    runServer {
        minecraftVersion("1.21.1")
        jvmArgs("-Dcom.mojang.eula.agree=true")
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
    apiVersion = "1.21"
    name = "Stardust"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    authors = listOf("theShadowsDust", "OneLiteFeather")
    serverDependencies {
        register("LuckPerms") {
            required = false
        }
        register("ProtocolLib") {
            required = false
        }
    }
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
