import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "com.willfp"
version = rootProject.version

dependencies {
    compileOnly(project(":eco-core:core-plugin"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn(publishToMavenLocal)
    }

    compileJava {
        options.release = 21
    }

    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}
