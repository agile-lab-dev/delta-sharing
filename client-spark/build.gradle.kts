plugins {
    java
    id("com.diffplug.spotless")
    id("whitefox.java-conventions")
}

group = "io.whitefox"
version = "spark-connector"

repositories {
    mavenCentral()
}

val hadoopVersion = "3.3.6"
dependencies {
    // OPENAPI
    implementation("org.eclipse.microprofile.openapi:microprofile-openapi-api:3.1.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    // DELTA
    testImplementation(String.format("org.apache.hadoop:hadoop-common:%s", hadoopVersion))
    testImplementation("io.delta:delta-sharing-spark_2.12:1.0.2")

    //SPARK
    testImplementation("org.apache.spark:spark-core_2.12:3.3.2")
    testImplementation("org.apache.spark:spark-sql_2.12:3.3.2")
    testImplementation("com.github.mrpowers:spark-fast-tests_2.12:1.3.0")

    //JUNIT
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")

}

// region code formatting
spotless {
    java {}
}
// endregion

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val openApiCodeGenDir = "generated/openapi"
val generatedCodeDirectory = generatedCodeDirectory(layout, openApiCodeGenDir)

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openapiGenerateClientApi") {
    generatorName.set("java")
    inputSpec.set("$rootDir/protocol/whitefox-protocol-api.yml")
    library.set("native")
    outputDir.set(generatedCodeDirectory)
    additionalProperties.set(mapOf(
            "apiPackage" to "io.whitefox.api.client",
            "invokerPackage" to "io.whitefox.api.utils",
            "modelPackage" to "io.whitefox.api.client.model",
            "dateLibrary" to "java8",
            "sourceFolder" to "src/gen/java",
            "openApiNullable" to "true",
            "annotationLibrary" to "none",
            "serializationLibrary" to "jackson",
            "useJakartaEe" to "true",
            "useRuntimeException" to "true"
    ))
}