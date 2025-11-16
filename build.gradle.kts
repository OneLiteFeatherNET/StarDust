import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    alias(libs.plugins.pluginYaml)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runServer)
    `maven-publish`
    jacoco
}

dependencies {

    compileOnly(libs.paper)
    compileOnly(libs.packetEvents)
    implementation(libs.bundles.cloud)
    compileOnly(libs.luckperms)

    implementation(libs.bundles.hibernate)
    implementation(libs.jaxbRuntime)
    implementation(libs.postgresql)
    implementation(libs.apacheCommons)
    compileOnly(libs.bluemapApi)

    // Testing
    testImplementation(platform(libs.mycelium.bom))
    testImplementation(libs.paper)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.mockbukkit)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("stardust.jar")
        mergeServiceFiles()
    }
    test {
        useJUnitPlatform()
        jvmArgs("-Dstardust.insideTest=true")
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy(rootProject.tasks.jacocoTestReport)
    }
    runServer {
        minecraftVersion("1.21.4")
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
    jacocoTestReport {
        dependsOn(rootProject.tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(true)
        }
    }
}

paper {
    main = "${rootProject.group}.stardust.StardustPlugin"
    apiVersion = "1.21"
    name = "Stardust"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    authors = listOf("theShadowsDust", "OneLiteFeather")

    val userPermissions = listOf("glow", "sign", "unsign", "help").map { "stardust.command.$it" }.toList();

    val staffPermissions = listOf("flight", "godmode", "heal", "rename", "repair", "skull", "vanish").map { "stardust.command.$it" }.toList()

    val otherCommandPermissions = listOf("flight", "glow", "godmode", "heal", "vanish").map { "stardust.command.$it.others" }.toList().toList()

    val adminPermissions = listOf("stardust.command.vanish.fakejoin", "stardust.command.vanish.fakequit", "stardust.command.frogbucket")

    permissions {

        this.register("stardust.commands.user") {
            children = userPermissions
            this.default = BukkitPluginDescription.Permission.Default.FALSE
        }

        this.register("stardust.commands.staff") {
            children = staffPermissions
            this.default = BukkitPluginDescription.Permission.Default.OP
        }

        this.register("stardust.bundle.vanish") {
            children = listOf(
                "stardust.command.vanish.toggleproperty",
                "stardust.vanish.silentopen.interact",
                "stardust.vanish.silentopen",
                "stardust.vanish.auto")
            this.default = BukkitPluginDescription.Permission.Default.OP
        }

        this.register("stardust.bundle.bypass") {
            children = listOf("stardust.commandcooldown.bypass", "stardust.bypass.damage.invulnerable", "stardust.command.sign.override")
            this.default = BukkitPluginDescription.Permission.Default.OP
        }

        this.register("stardust.commands.admin") {
            children = otherCommandPermissions.plus(adminPermissions)
            this.default = BukkitPluginDescription.Permission.Default.OP
        }
    }

    serverDependencies {
        register("LuckPerms") {
            required = false
        }

        register("BlueMap") {
            required = false
        }

        register("packetevents") {
            required = true
        }
    }

}

publishing {
    publications.create<MavenPublication>("maven") {
        artifact(project.tasks.getByName("shadowJar"))
        version = rootProject.version as String
        artifactId = "stardust"
        groupId = rootProject.group as String
        pom {
            name = "Stardust"
            description = "A simple essential plugin for OneLiteFeather servers, providing basic features and utilities."
            url = "https://github.com/OneLiteFeatherNET/stardust"
            licenses {
                license {
                    name = "AGPL-3.0"
                    url = "https://www.gnu.org/licenses/agpl-3.0.en.html"
                }
            }
            developers {
                developer {
                    id = "theShadowsDust"
                    name = "theShadowsDust"
                    email = "theShadowDust@onelitefeather.net"
                }
                developer {
                    id = "themeinerlp"
                    name = "Phillipp Glanz"
                    email = "p.glanz@madfix.me"
                }
            }
            scm {
                connection = "scm:git:git://github.com:OneLiteFeatherNET/StarDust.git"
                developerConnection = "scm:git:ssh://git@github.com:OneLiteFeatherNET/StarDust.git"
                url = "https://github.com/OneLiteFeatherNET/StarDust"
            }
        }
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            }

            name = "OneLiteFeatherRepository"
            val releasesRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-releases")
            val snapshotsRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-snapshots")
            url = if (version.toString().contains("SNAPSHOT") || version.toString().contains("BETA") || version.toString().contains("ALPHA")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}