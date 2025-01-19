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

            version("hibernate", "6.6.5.Final")
            version("paper", "1.21.1-R0.1-SNAPSHOT")
            version("luckperms", "5.4")
            version("protocolLib", "5.0.0")
            version("jaxbRuntime", "4.0.5")
            version("postgresql", "42.7.3")
            version("apacheCommons", "3.17.0")
            version("junitApi", "5.11.0")

            plugin("pluginYaml", "net.minecrell.plugin-yml.paper").version("0.6.0")
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")
            plugin("runServer", "xyz.jpenilla.run-paper").version("2.3.1")
            plugin("publishData", "de.chojo.publishdata").version("1.4.0")


            library("paper", "io.papermc.paper", "paper-api").versionRef("paper")
            library("luckperms", "net.luckperms", "api").versionRef("luckperms")
            library("protocolLib", "com.comphenix.protocol", "ProtocolLib").versionRef("protocolLib")

            library("cloudPaper", "org.incendo", "cloud-paper").version("2.0.0-beta.10")
            library("cloudAnnotations", "org.incendo", "cloud-annotations").version("2.0.0")
            library("cloudExtras", "org.incendo", "cloud-minecraft-extras").version("2.0.0-beta.10")

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
