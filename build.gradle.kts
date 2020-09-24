plugins {
    kotlin("jvm") version "1.4.10"
    id("com.jfrog.bintray") version "1.8.5"
    `maven-publish`
    `java-library`
}

repositories {
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/manami-project/maven")
    }
}

group = "io.github.manamiproject"
version = project.findProperty("releaseVersion") as String? ?: ""
val projectName = "modb-anilist"

dependencies {
    api("io.github.manamiproject:modb-core:2.3.2")
    api(kotlin("stdlib-jdk8"))

    implementation(platform(kotlin("bom")))
    implementation("org.jsoup:jsoup:1.13.1")

    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("io.github.manamiproject:modb-test:1.0.4")
}

kotlin {
    explicitApi()
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = Versions.JVM_TARGET
    freeCompilerArgs = listOf("-Xinline-classes")
}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = Versions.JVM_TARGET
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports.html.isEnabled = false
    reports.junitXml.isEnabled = false
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

object Versions {
    const val JVM_TARGET = "11"
}

bintray {
    user = project.findProperty("bintrayUser") as String? ?: ""
    key = project.findProperty("bintrayApiKey") as String? ?: ""

    setPublications("maven")

    with(pkg) {
        repo = "maven"
        name = projectName
        with(version) {
            name = project.version.toString()
            vcsTag = project.version.toString()
        }
        setLicenses("AGPL-V3")
        vcsUrl = "https://github.com/manami-project/$projectName"
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javaDoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = projectName
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javaDoc.get())

            pom {
                packaging = "jar"
                name.set(projectName)
                description.set("This lib contains downloader and converter for downloading raw data from anilist.co and convert it to an anime object.")
                url.set("https://github.com/manami-project/$projectName")

                licenses {
                    license {
                        name.set("AGPL-V3")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:manami-project/$projectName.git")
                    developerConnection.set("scm:git:ssh://github.com:manami-project/$projectName.git")
                    url.set("https://github.com/manami-project/$projectName")
                }
            }
        }
    }
}