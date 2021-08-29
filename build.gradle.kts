import java.io.ByteArrayOutputStream

plugins {
    `java-library`
    `maven-publish`

    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm") version "1.5.21"
}

group = "com.github.natanbc"
version = Version(major = 0, minor = 8, revision = 0)

repositories {
    maven("https://dimensional.jfrog.io/artifactory/maven")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("com.github.natanbc:native-loader:0.7.0")

    compileOnly("com.sedmelluq:lavaplayer:1.4.4")

    testImplementation("com.sedmelluq:lavaplayer:1.4.0")
    testImplementation("ch.qos.logback:logback-classic:1.2.5")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

tasks.jar {
    archiveBaseName.set(project.name)
    manifest {
        attributes["Implementation-Version"] = project.version
    }
}

tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
}

tasks.shadowJar {
    archiveClassifier.set("withDependencies")
    dependsOn("sourcesJar")
}

tasks.publish {
    dependsOn("shadowJar")
    onlyIf {
        System.getenv("JFROG_USERNAME") != null&& System.getenv("JFROG_PASSWORD") != null
    }
}

tasks.compileKotlin {
    sourceCompatibility = "16"
    targetCompatibility = "16"
    kotlinOptions.jvmTarget = "16"
}

/* write version task */
fun getBuildVersion(): String {
    val gitVersion = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = gitVersion
    }

    return "$version\n${gitVersion.toString().trim()}"
}

tasks.create("writeVersion") {
    val resourcePath = sourceSets["main"].resources.srcDirs.first()
    val resource = file(resourcePath)

    if (!resource.exists()) {
        resourcePath.mkdirs()
    }

    file("$resourcePath/version.txt").writeText(getBuildVersion())
}

publishing {
    repositories {
        maven("https://dimensional.jfrog.io/artifactory/maven") {
            name = "jfrog"
            credentials {
                username = System.getenv("JFROG_USERNAME")
                password = System.getenv("JFROG_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("jfrog") {
            from(components["java"])

            groupId = group as String
            version = project.version.toString()
            artifactId = "lavadsp"

            artifact("sourcesJar") {
                classifier = "sources"
            }
        }
    }
}

fun getProjectProperty(propertyName: String): String {
    return if (hasProperty(propertyName)) project.properties[propertyName].toString() else System.getenv(propertyName)
        ?: ""
}

data class Version(val major: Int, val minor: Int, val revision: Int) {
    override fun toString(): String {
        return "${major}.${minor}" + (if (revision == 0) "" else ".${revision}")
    }
}

