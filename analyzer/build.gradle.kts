plugins {
    id("printscript.java-conventions")
}

dependencies {
    api(project(":ast"))
    // AnalyzerConfigLoader implements ConfigLoader<AnalyzerConfig> - parte de la API publica.
    api(project(":config"))

    // Carga de configuracion en YAML/JSON para las reglas de analisis estatico.
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}
