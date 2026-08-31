plugins {
    id("printscript.java-conventions")
    id("printscript.published-library")
}

dependencies {
    api(project(":ast"))
    // FormatterConfigLoader implements ConfigLoader<FormatterConfig> - parte de la API publica.
    api(project(":config"))

    // Carga de configuracion en YAML/JSON para las reglas de formateo.
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}
