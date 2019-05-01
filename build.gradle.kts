/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java Library project to get you started.
 * For more details take a look at the Java Libraries chapter in the Gradle
 * User Manual available at https://docs.gradle.org/5.4/userguide/java_library_plugin.html
 */

group = "com.github.juliomarcopineda"
version = "0.1.0"

plugins {
    `java-library`
	`maven-publish`
	signing
}

repositories {
    jcenter()
	mavenCentral()
}

dependencies {	
    testImplementation("junit:junit:4.12")
	testImplementation("org.xerial:sqlite-jdbc:3.27.2.1")
}

tasks {
    jar {
        manifest {
            attributes(
                mapOf("Implementation-Title" to project.name,
                      "Implementation-Version" to project.version)
            )
        }
    }
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

tasks.test {
	useJUnit()
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = "jdbc-stream"
			from(components["java"])
			artifact(tasks["sourcesJar"])
			artifact(tasks["javadocJar"])
			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
			pom {
				name.set("JDBC Stream")
				description.set("Light-weight library to wrap JDBC ResultSet to Java 8 Stream")
				url.set("https://github.com/juliomarcopineda/jdbc-stream")
				licenses {
					license {
						name.set("MIT License")
						url.set("https://opensource.org/licenses/MIT")
					}
				}
				developers {
					developer {
						id.set("juliomarcopineda")
						name.set("Julio Marco Pineda")
						email.set("juliomarcopineda@gmail.com")
					}
				}
				scm {
					connection.set("git@github.com:juliomarcopineda/jdbc-stream.git")
                    developerConnection.set("git@github.com:juliomarcopineda/jdbc-stream.git")
					url.set("https://github.com/juliomarcopineda/jdbc-stream")
				}
			}
		}
	}
	
	repositories {
		maven {
			url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
			credentials {
				username = sonatypeUsername
				password = sonatypePassword
			}
		}
	}
}

signing {
    sign(publishing.publications["mavenJava"])
}
