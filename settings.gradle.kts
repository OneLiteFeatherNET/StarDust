import org.gradle.kotlin.dsl.mavenCentral
import org.gradle.kotlin.dsl.repositories

rootProject.name = "stardust"
dependencyResolutionManagement {

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven {
            name = "OneLiteFeatherRepository"
            url = uri("https://repo.onelitefeather.dev/onelitefeather")
            if (System.getenv("CI") != null) {
                credentials {
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            } else {
                credentials(PasswordCredentials::class)
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
    versionCatalogs {
        create("libs") {

            version("hibernate", "7.0.7.Final")
            version("paper", "1.21.4-R0.1-SNAPSHOT")
            version("luckperms", "5.5")
            version("protocolLib", "5.3.0")
            version("jaxbRuntime", "4.0.5")
            version("postgresql", "42.7.7")
            version("apacheCommons", "3.18.0")
            version("junitApi", "5.11.0")
            version("bom", "1.4.2")
            version("mockbukit", "4.72.2")

            plugin("pluginYaml", "net.minecrell.plugin-yml.paper").version("0.6.0")
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")
            plugin("runServer", "xyz.jpenilla.run-paper").version("2.3.1")


            library("mycelium.bom", "net.onelitefeather", "mycelium-bom").versionRef("bom")
            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("luckperms", "net.luckperms", "api").versionRef("luckperms")
            library("protocolLib", "com.comphenix.protocol", "ProtocolLib").versionRef("protocolLib")

            library("cloudPaper", "org.incendo", "cloud-paper").version("2.0.0-SNAPSHOT")
            library("cloudAnnotations", "org.incendo", "cloud-annotations").version("2.0.0")
            library("cloudExtras", "org.incendo", "cloud-minecraft-extras").version("2.0.0-SNAPSHOT")

            //Database
            library("hibernateCore", "org.hibernate", "hibernate-core").versionRef("hibernate")
            library("hibernateHikariCP", "org.hibernate", "hibernate-hikaricp").versionRef("hibernate")
            library("jaxbRuntime", "org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxbRuntime")
            library("postgresql", "org.postgresql", "postgresql").versionRef("postgresql")

            library("apacheCommons", "org.apache.commons", "commons-lang3").versionRef("apacheCommons")

            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").withoutVersion()
            library("junit-jupiter-engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("junit.platform.launcher", "org.junit.platform", "junit-platform-launcher").withoutVersion()
            library("mockbukkit", "org.mockbukkit.mockbukkit", "mockbukkit-v1.21").versionRef("mockbukit")

            bundle("cloud", listOf("cloudPaper", "cloudAnnotations", "cloudExtras"))
            bundle("hibernate", listOf("hibernateCore", "hibernateHikariCP"))
        }
    }
}
