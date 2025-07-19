plugins {
    id("java")
    alias(libs.plugins.pluginYaml)
    alias(libs.plugins.shadow)
    alias(libs.plugins.runServer)
    `maven-publish`
}
dependencies {

    compileOnly(libs.paper)
    implementation(libs.bundles.cloud)
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
    }
    runServer {
        minecraftVersion("1.21.4")
        jvmArgs("-Dcom.mojang.eula.agree=true")
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