// Define Java conventions for this organization.
plugins {
    java
    jacoco
    id("com.palantir.git-version")
    id("org.openapi.generator")
    id("com.diffplug.spotless")
}
// Projects should use Maven Central for external dependencies
repositories {
    mavenCentral()
    mavenLocal()
}

// Enable deprecation messages when compiling Java code
tasks.withType<JavaCompile>().configureEach {
    // example for javac args
    // options.compilerArgs.add("-Xlint:deprecation")
}
spotless {
    java {
        importOrder()
        removeUnusedImports()
        palantirJavaFormat().style("GOOGLE")
        formatAnnotations()
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.check) // tests are required to run before generating the report
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}
val gitVersion: groovy.lang.Closure<String> by extra
group = "io.whitefox"
version = gitVersion()