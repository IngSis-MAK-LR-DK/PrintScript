/*
 * Convention plugin para los módulos que se publican como artefactos Maven en GitHub Packages.
 * Lo aplican solo las librerías (common, ast, config, lexer, parser, interpreter, formatter,
 * analyzer) — NO cli (es una aplicación, no una librería) ni plugins:modulo-operator (es un
 * plugin de ejemplo, no core).
 *
 * groupId/version salen de `allprojects {}` en el build.gradle.kts raíz; el artifactId es,
 * por defecto, el nombre del subproyecto de Gradle (ej. "common", "interpreter", ...).
 */

plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/IngSis-MAK-LR-DK/PrintScript")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
