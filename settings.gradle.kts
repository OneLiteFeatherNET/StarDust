rootProject.name = "stardust"


pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {

            version("cloud", "1.8.4")
            version("hibernate", "6.6.0.Final")
            version("commodore", "2.2")
            version("paper", "1.20.6-R0.1-SNAPSHOT")
            version("luckperms", "5.4")
            version("protocolLib", "5.0.0")
            version("jaxbRuntime", "4.0.2")
            version("postgresql", "42.7.3")
            version("apacheCommons", "3.16.0")
            version("junitApi", "5.11.0")

            plugin("pluginYaml", "net.minecrell.plugin-yml.paper").version("0.6.0")
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")
            plugin("runServer", "xyz.jpenilla.run-paper").version("2.1.0")
            plugin("publishData", "de.chojo.publishdata").version("1.4.0")


            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("luckperms", "net.luckperms", "api").versionRef("luckperms")
            library("protocolLib", "com.comphenix.protocol", "ProtocolLib").versionRef("protocolLib")

            library("cloudPaper", "cloud.commandframework", "cloud-paper").versionRef("cloud")
            library("cloudAnnotations", "cloud.commandframework", "cloud-annotations").versionRef("cloud")
            library("cloudExtras", "cloud.commandframework", "cloud-minecraft-extras").versionRef("cloud")
            library("commodore", "me.lucko", "commodore").versionRef("commodore")

            //Database
            library("hibernateCore", "org.hibernate", "hibernate-core").versionRef("hibernate")
            library("hibernateHikariCP", "org.hibernate", "hibernate-hikaricp").versionRef("hibernate")
            library("jaxbRuntime", "org.glassfish.jaxb", "jaxb-runtime").versionRef("jaxbRuntime")
            library("postgresql", "org.postgresql", "postgresql").versionRef("postgresql")

            library("apacheCommons", "org.apache.commons", "commons-lang3").versionRef("apacheCommons")

            library("junitApi", "org.junit.jupiter", "junit-jupiter-api").versionRef("junitApi")
            library("junitEngine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()

            bundle("cloud", listOf("cloudPaper", "cloudAnnotations", "cloudExtras"))
            bundle("hibernate", listOf("hibernateCore", "hibernateHikariCP"))
        }
    }
}
