plugins {
    id("printscript.java-conventions")
}

dependencies {
    api(project(":common"))
    api(project(":parser"))

    // Carga de configuracion en YAML/JSON para las reglas de formateo.
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}
