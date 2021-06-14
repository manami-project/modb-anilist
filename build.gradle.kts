plugins {
    kotlin("jvm") version "1.5.10"
    `maven-publish`
    `java-library`
}

val projectName = "modb-anilist"
val githubUsername = "manami-project"

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/$githubUsername/$projectName")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: githubUsername
            password = project.findProperty("gpr.key") as String? ?: ""
        }
    }
}

group = "io.github.manamiproject"
version = project.findProperty("release.version") as String? ?: ""

dependencies {
    api("io.github.manamiproject:modb-core:5.0.1")
    api(kotlin("stdlib-jdk8"))

    implementation(platform(kotlin("bom")))
    implementation("org.jsoup:jsoup:1.13.1")

    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("io.github.manamiproject:modb-test:1.2.5")
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javaDoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$githubUsername/$projectName")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: githubUsername
                password = project.findProperty("gpr.key") as String? ?: ""
            }
        }
    }
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
                url.set("https://github.com/$githubUsername/$projectName")

                licenses {
                    license {
                        name.set("AGPL-V3")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.txt")
                    }
                }

                scm {
                    connection.set("scm:git@github.com:$githubUsername/$projectName.git")
                    developerConnection.set("scm:git:ssh://github.com:$githubUsername/$projectName.git")
                    url.set("https://github.com/$githubUsername/$projectName")
                }
            }
        }
    }
}